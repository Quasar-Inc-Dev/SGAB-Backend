package sgab.sgab.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgab.sgab.events.EmprestimoRegistradoEvent;
import sgab.sgab.events.PrazoDevolucaoProximoEvent;
import sgab.sgab.repositories.EmprestimoRepository;
import sgab.sgab.repositories.ExemplarRepository;
import sgab.sgab.repositories.LivroRepository;
import sgab.sgab.repositories.LeitorRepository;
import sgab.sgab.dtos.response.EmprestimoResponseDTO;
import sgab.sgab.entities.Emprestimo;
import sgab.sgab.entities.Exemplar;
import sgab.sgab.entities.Livro;
import sgab.sgab.entities.Leitor;
import sgab.sgab.entities.Reserva;
import sgab.sgab.entities.Enum.StatusExemplar;
import sgab.sgab.exceptions.LivroNaoEncontradoException;
import sgab.sgab.exceptions.RecursoNaoEncontradoException;

@Service
public class EmprestimoService {

    private static final int DIAS_DE_EMPRESTIMO = 7;

    private final EmprestimoRepository emprestimoRepository;
    private final LivroRepository livroRepository;
    private final LeitorRepository leitorRepository;
    private final ExemplarRepository exemplarRepository;
    private final ReservaService reservaService;
    private final ApplicationEventPublisher eventPublisher;

    // Com quantos dias de antecedência o leitor recebe o lembrete de devolução
    private final int diasAntecedenciaLembrete;

    public EmprestimoService(
            EmprestimoRepository emprestimoRepository,
            LivroRepository livroRepository,
            LeitorRepository leitorRepository,
            ExemplarRepository exemplarRepository,
            ReservaService reservaService,
            ApplicationEventPublisher eventPublisher,
            @Value("${sgab.emprestimo.dias-antecedencia-lembrete:2}") int diasAntecedenciaLembrete
    ) {
        this.emprestimoRepository = emprestimoRepository;
        this.livroRepository = livroRepository;
        this.leitorRepository = leitorRepository;
        this.exemplarRepository = exemplarRepository;
        this.reservaService = reservaService;
        this.eventPublisher = eventPublisher;
        this.diasAntecedenciaLembrete = diasAntecedenciaLembrete;
    }

    public List<EmprestimoResponseDTO> listarTodos() {
        return emprestimoRepository.findAll()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public EmprestimoResponseDTO buscarPorId(Integer id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    public EmprestimoResponseDTO registrarEmprestimo(Integer livroId, Integer leitorId) {
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new LivroNaoEncontradoException("Livro não encontrado."));

        Leitor leitor = leitorRepository.findById(leitorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Leitor não encontrado."));

        if ("INATIVO".equals(livro.getLivroStatus())) {
            throw new IllegalArgumentException("O livro está desativado e não pode ser emprestado.");
        }

        Exemplar exemplar = escolherExemplar(livroId, leitorId);

        return converterParaResponse(criarEmprestimo(exemplar, leitor));
    }

    /**
     * Cria o empréstimo de um exemplar que já foi separado para o leitor (retirada no balcão).
     * O prazo de devolução começa a contar a partir deste momento.
     */
    @Transactional
    public Emprestimo registrarRetirada(Exemplar exemplar, Leitor leitor) {
        if (exemplar.getStatus() != StatusExemplar.RESERVADO) {
            throw new IllegalArgumentException("O exemplar separado não está mais reservado para retirada.");
        }

        return criarEmprestimo(exemplar, leitor);
    }

    private Emprestimo criarEmprestimo(Exemplar exemplar, Leitor leitor) {
        exemplar.setStatus(StatusExemplar.EMPRESTADO);
        exemplarRepository.save(exemplar);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setExemplar(exemplar);
        emprestimo.setLeitor(leitor);

        LocalDateTime dataEmprestimo = LocalDateTime.now();
        emprestimo.setDataEmprestimo(dataEmprestimo);
        emprestimo.setDataDevolucaoPrevista(dataEmprestimo.plusDays(DIAS_DE_EMPRESTIMO));
        emprestimo.setStatusDevolucao(false);

        Emprestimo salvo = emprestimoRepository.save(emprestimo);

        eventPublisher.publishEvent(new EmprestimoRegistradoEvent(
                leitor.getUsuario().getEmail(), leitor.getUsuario().getNome(),
                exemplar.getLivro().getTitulo(), salvo.getDataDevolucaoPrevista()
        ));

        return salvo;
    }

    @Transactional
    public EmprestimoResponseDTO renovar(Integer id) {
        Emprestimo emprestimo = buscarEntidadePorId(id);

        if (Boolean.TRUE.equals(emprestimo.getStatusDevolucao())) {
            throw new IllegalArgumentException("Renovação negada: o empréstimo já foi devolvido.");
        }

        if (reservaService.exemplarPossuiFilaDeEspera(emprestimo.getExemplar().getExemplarId())) {
            throw new IllegalArgumentException("Renovação não permitida: o exemplar possui reservas pendentes na fila de espera.");
        }

        if (emprestimo.getDataDevolucaoPrevista().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Renovação negada: o empréstimo está com prazo vencido.");
        }

        emprestimo.setDataDevolucaoPrevista(emprestimo.getDataDevolucaoPrevista().plusDays(DIAS_DE_EMPRESTIMO));
        // Nova data de devolução: o leitor deve receber um novo lembrete
        emprestimo.setLembretePrazoEnviado(false);

        return converterParaResponse(emprestimoRepository.save(emprestimo));
    }

    @Transactional
    public EmprestimoResponseDTO devolver(Integer id) {
        Emprestimo emprestimo = buscarEntidadePorId(id);

        if (Boolean.TRUE.equals(emprestimo.getStatusDevolucao())) {
            throw new IllegalArgumentException("Este empréstimo já foi devolvido.");
        }

        emprestimo.setDataDevolucao(LocalDateTime.now());
        emprestimo.setStatusDevolucao(true);
        emprestimoRepository.save(emprestimo);

        // O exemplar vai para o primeiro da fila de reservas ou volta a ficar disponível
        reservaService.destinarExemplarLiberado(emprestimo.getExemplar());

        return converterParaResponse(emprestimo);
    }

    /**
     * Envia o lembrete dos empréstimos em aberto que vencem nos próximos dias.
     * Chamado periodicamente pelo agendador; cada empréstimo recebe um lembrete por data de devolução.
     */
    @Transactional
    public int enviarLembretesPrazoDevolucao() {
        LocalDateTime agora = LocalDateTime.now();
        List<Emprestimo> proximosDoVencimento = emprestimoRepository.buscarSemLembreteComVencimentoEntre(
                agora, agora.plusDays(diasAntecedenciaLembrete)
        );

        for (Emprestimo emprestimo : proximosDoVencimento) {
            emprestimo.setLembretePrazoEnviado(true);
            emprestimoRepository.save(emprestimo);

            Leitor leitor = emprestimo.getLeitor();
            eventPublisher.publishEvent(new PrazoDevolucaoProximoEvent(
                    leitor.getUsuario().getEmail(), leitor.getUsuario().getNome(),
                    emprestimo.getExemplar().getLivro().getTitulo(), emprestimo.getDataDevolucaoPrevista()
            ));
        }

        return proximosDoVencimento.size();
    }

    /**
     * Se o leitor tem um exemplar separado por reserva, usa esse exemplar e conclui a reserva.
     * Caso contrário, pega um exemplar disponível.
     */
    private Exemplar escolherExemplar(Integer livroId, Integer leitorId) {
        Optional<Reserva> reservaDoLeitor = reservaService.buscarReservaAguardandoRetirada(livroId, leitorId);

        if (reservaDoLeitor.isPresent()) {
            Reserva reserva = reservaDoLeitor.get();
            reservaService.marcarComoAtendida(reserva);
            return reserva.getExemplar();
        }

        return exemplarRepository
                .findFirstByLivroLivroIdAndStatusOrderByExemplarIdAsc(livroId, StatusExemplar.DISPONIVEL)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Não há exemplares disponíveis deste livro. O leitor pode fazer uma reserva."
                ));
    }

    private Emprestimo buscarEntidadePorId(Integer id) {
        return emprestimoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empréstimo não encontrado."));
    }

    private EmprestimoResponseDTO converterParaResponse(Emprestimo emprestimo) {
        Exemplar exemplar = emprestimo.getExemplar();

        return new EmprestimoResponseDTO(
                emprestimo.getEmprestimoId(),
                exemplar.getExemplarId(),
                exemplar.getTombo(),
                exemplar.getLivro().getLivroId(),
                exemplar.getLivro().getTitulo(),
                emprestimo.getLeitor().getId(),
                emprestimo.getLeitor().getUsuario().getNome(),
                emprestimo.getDataEmprestimo(),
                emprestimo.getDataDevolucaoPrevista(),
                emprestimo.getDataDevolucao(),
                emprestimo.getStatusDevolucao()
        );
    }
}
