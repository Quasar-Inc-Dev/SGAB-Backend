package sgab.sgab.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sgab.sgab.dtos.response.ReservaResponseDTO;
import sgab.sgab.dtos.response.SolicitacaoResponseDTO;
import sgab.sgab.entities.Emprestimo;
import sgab.sgab.entities.Exemplar;
import sgab.sgab.entities.Leitor;
import sgab.sgab.entities.Livro;
import sgab.sgab.entities.Solicitacao;
import sgab.sgab.entities.Usuario;
import sgab.sgab.entities.Enum.StatusExemplar;
import sgab.sgab.entities.Enum.StatusSolicitacao;
import sgab.sgab.entities.Enum.TipoSolicitacao;
import sgab.sgab.exceptions.LivroNaoEncontradoException;
import sgab.sgab.exceptions.RecursoNaoEncontradoException;
import sgab.sgab.repositories.ExemplarRepository;
import sgab.sgab.repositories.LeitorRepository;
import sgab.sgab.repositories.LivroRepository;
import sgab.sgab.repositories.ReservaRepository;
import sgab.sgab.repositories.SolicitacaoRepository;

@Service
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final LeitorRepository leitorRepository;
    private final LivroRepository livroRepository;
    private final ExemplarRepository exemplarRepository;
    private final ReservaRepository reservaRepository;
    private final EmprestimoService emprestimoService;
    private final ReservaService reservaService;

    // Prazo para o leitor retirar no balcão o exemplar separado na aprovação
    private final int diasParaRetirada;

    public SolicitacaoService(
            SolicitacaoRepository solicitacaoRepository,
            LeitorRepository leitorRepository,
            LivroRepository livroRepository,
            ExemplarRepository exemplarRepository,
            ReservaRepository reservaRepository,
            EmprestimoService emprestimoService,
            ReservaService reservaService,
            @Value("${sgab.solicitacao.dias-para-retirada:3}") int diasParaRetirada
    ) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.leitorRepository = leitorRepository;
        this.livroRepository = livroRepository;
        this.exemplarRepository = exemplarRepository;
        this.reservaRepository = reservaRepository;
        this.emprestimoService = emprestimoService;
        this.reservaService = reservaService;
        this.diasParaRetirada = diasParaRetirada;
    }

    @Transactional
    public SolicitacaoResponseDTO solicitarEmprestimo(Integer livroId, Usuario usuarioLogado) {
        Leitor leitor = buscarLeitor(usuarioLogado);

        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new LivroNaoEncontradoException("Livro não encontrado."));

        validarLivroAtivo(livro);

        if (solicitacaoRepository.existsByLeitorIdAndLivroLivroIdAndTipoAndStatus(
                leitor.getId(), livroId, TipoSolicitacao.EMPRESTIMO, StatusSolicitacao.PENDENTE)) {
            throw new IllegalArgumentException("Você já tem uma solicitação de empréstimo pendente para este livro.");
        }

        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setTipo(TipoSolicitacao.EMPRESTIMO);
        solicitacao.setLeitor(leitor);
        solicitacao.setLivro(livro);

        return converterParaResponse(solicitacaoRepository.save(solicitacao));
    }

    @Transactional
    public SolicitacaoResponseDTO solicitarReserva(Integer exemplarId, Usuario usuarioLogado) {
        Leitor leitor = buscarLeitor(usuarioLogado);

        Exemplar exemplar = exemplarRepository.findById(exemplarId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Exemplar não encontrado."));

        validarLivroAtivo(exemplar.getLivro());

        if (exemplar.getStatus() == StatusExemplar.MANUTENCAO || exemplar.getStatus() == StatusExemplar.EXTRAVIADO) {
            throw new IllegalArgumentException("Este exemplar não está em circulação e não pode ser reservado.");
        }

        if (solicitacaoRepository.existsByLeitorIdAndExemplarExemplarIdAndTipoAndStatus(
                leitor.getId(), exemplarId, TipoSolicitacao.RESERVA, StatusSolicitacao.PENDENTE)) {
            throw new IllegalArgumentException("Você já tem uma solicitação de reserva pendente para este exemplar.");
        }

        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setTipo(TipoSolicitacao.RESERVA);
        solicitacao.setLeitor(leitor);
        solicitacao.setLivro(exemplar.getLivro());
        solicitacao.setExemplar(exemplar);

        return converterParaResponse(solicitacaoRepository.save(solicitacao));
    }

    public List<SolicitacaoResponseDTO> listarPorStatus(StatusSolicitacao status, TipoSolicitacao tipo) {
        List<Solicitacao> solicitacoes = tipo == null
                ? solicitacaoRepository.findByStatusOrderByDataSolicitacaoAsc(status)
                : solicitacaoRepository.findByStatusAndTipoOrderByDataSolicitacaoAsc(status, tipo);

        return solicitacoes.stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public List<SolicitacaoResponseDTO> listarDoLeitor(Usuario usuarioLogado) {
        Leitor leitor = buscarLeitor(usuarioLogado);

        return solicitacaoRepository.findByLeitorIdOrderByDataSolicitacaoDesc(leitor.getId())
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    /**
     * EMPRESTIMO: separa um exemplar disponível e aguarda o leitor ir ao balcão (o empréstimo
     * só é criado na confirmação da retirada, quando o prazo começa a contar).
     * RESERVA: cria a reserva na hora, com as regras de negócio da reserva.
     * Se alguma regra impedir (ex.: sem exemplar disponível), nada é salvo e a solicitação continua pendente.
     */
    @Transactional
    public SolicitacaoResponseDTO aprovar(Integer id, Usuario analista) {
        Solicitacao solicitacao = buscarComStatus(id, StatusSolicitacao.PENDENTE,
                "Esta solicitação já foi analisada ou cancelada.");

        if (solicitacao.getTipo() == TipoSolicitacao.EMPRESTIMO) {
            validarLivroAtivo(solicitacao.getLivro());

            Exemplar exemplar = exemplarRepository
                    .findFirstByLivroLivroIdAndStatusOrderByExemplarIdAsc(
                            solicitacao.getLivro().getLivroId(), StatusExemplar.DISPONIVEL)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Não há exemplares disponíveis deste livro. O leitor pode fazer uma reserva."
                    ));

            exemplar.setStatus(StatusExemplar.RESERVADO);
            exemplarRepository.save(exemplar);

            solicitacao.setExemplar(exemplar);
            solicitacao.setDataLimiteRetirada(LocalDateTime.now().plusDays(diasParaRetirada));
            registrarAnalise(solicitacao, StatusSolicitacao.AGUARDANDO_RETIRADA, analista);
        } else {
            ReservaResponseDTO reserva = reservaService.solicitarReserva(
                    solicitacao.getExemplar().getExemplarId(), solicitacao.getLeitor().getId()
            );
            solicitacao.setReserva(reservaRepository.getReferenceById(reserva.reservaId()));
            registrarAnalise(solicitacao, StatusSolicitacao.APROVADA, analista);
        }

        return converterParaResponse(solicitacaoRepository.save(solicitacao));
    }

    @Transactional
    public SolicitacaoResponseDTO confirmarRetirada(Integer id, Usuario responsavel) {
        Solicitacao solicitacao = buscarComStatus(id, StatusSolicitacao.AGUARDANDO_RETIRADA,
                "Esta solicitação não está aguardando retirada.");

        // A expiração em si (status EXPIRADA e liberação do exemplar) é feita pela verificação agendada
        if (solicitacao.getDataLimiteRetirada().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("O prazo de retirada expirou.");
        }

        Emprestimo emprestimo = emprestimoService.registrarRetirada(solicitacao.getExemplar(), solicitacao.getLeitor());

        solicitacao.setEmprestimo(emprestimo);
        solicitacao.setStatus(StatusSolicitacao.APROVADA);
        solicitacao.setDataRetirada(emprestimo.getDataEmprestimo());
        solicitacao.setRetiradaConfirmadaPor(responsavel);

        return converterParaResponse(solicitacaoRepository.save(solicitacao));
    }

    @Transactional
    public SolicitacaoResponseDTO recusar(Integer id, Usuario analista, String motivo) {
        Solicitacao solicitacao = buscarComStatus(id, StatusSolicitacao.PENDENTE,
                "Esta solicitação já foi analisada ou cancelada.");

        solicitacao.setMotivoRecusa(motivo);
        registrarAnalise(solicitacao, StatusSolicitacao.RECUSADA, analista);

        return converterParaResponse(solicitacaoRepository.save(solicitacao));
    }

    @Transactional
    public SolicitacaoResponseDTO cancelar(Integer id, Usuario usuarioLogado) {
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação não encontrada."));

        if (!solicitacao.getLeitor().getId().equals(usuarioLogado.getId())) {
            throw new IllegalArgumentException("Você só pode cancelar as suas próprias solicitações.");
        }

        boolean exemplarSeparado = solicitacao.getStatus() == StatusSolicitacao.AGUARDANDO_RETIRADA;

        if (solicitacao.getStatus() != StatusSolicitacao.PENDENTE && !exemplarSeparado) {
            throw new IllegalArgumentException("Só é possível cancelar solicitações pendentes ou aguardando retirada.");
        }

        solicitacao.setStatus(StatusSolicitacao.CANCELADA);
        solicitacaoRepository.save(solicitacao);

        // Devolve o exemplar separado para a fila de reservas ou para disponível
        if (exemplarSeparado) {
            reservaService.destinarExemplarLiberado(solicitacao.getExemplar());
        }

        return converterParaResponse(solicitacao);
    }

    /**
     * Marca como EXPIRADA as solicitações cujo prazo de retirada passou e libera o exemplar separado.
     * Chamado periodicamente pelo agendador.
     */
    @Transactional
    public int expirarRetiradasVencidas() {
        List<Solicitacao> vencidas = solicitacaoRepository.findByStatusAndDataLimiteRetiradaBefore(
                StatusSolicitacao.AGUARDANDO_RETIRADA, LocalDateTime.now()
        );

        for (Solicitacao solicitacao : vencidas) {
            solicitacao.setStatus(StatusSolicitacao.EXPIRADA);
            solicitacaoRepository.save(solicitacao);
            reservaService.destinarExemplarLiberado(solicitacao.getExemplar());
        }

        return vencidas.size();
    }

    private void registrarAnalise(Solicitacao solicitacao, StatusSolicitacao status, Usuario analista) {
        solicitacao.setStatus(status);
        solicitacao.setDataAnalise(LocalDateTime.now());
        solicitacao.setAnalisadoPor(analista);
    }

    private Solicitacao buscarComStatus(Integer id, StatusSolicitacao statusEsperado, String mensagemErro) {
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação não encontrada."));

        if (solicitacao.getStatus() != statusEsperado) {
            throw new IllegalArgumentException(mensagemErro);
        }

        return solicitacao;
    }

    private Leitor buscarLeitor(Usuario usuario) {
        // O id do Leitor é o mesmo id do Usuario (@MapsId)
        return leitorRepository.findById(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Apenas leitores podem fazer solicitações."));
    }

    private void validarLivroAtivo(Livro livro) {
        if ("INATIVO".equals(livro.getLivroStatus())) {
            throw new IllegalArgumentException("O livro está desativado.");
        }
    }

    private SolicitacaoResponseDTO converterParaResponse(Solicitacao solicitacao) {
        Exemplar exemplar = solicitacao.getExemplar();
        Usuario analista = solicitacao.getAnalisadoPor();

        return new SolicitacaoResponseDTO(
                solicitacao.getSolicitacaoId(),
                solicitacao.getTipo(),
                solicitacao.getStatus(),
                solicitacao.getLeitor().getId(),
                solicitacao.getLeitor().getUsuario().getNome(),
                solicitacao.getLeitor().getTipoLeitor(),
                solicitacao.getLivro().getLivroId(),
                solicitacao.getLivro().getTitulo(),
                exemplar == null ? null : exemplar.getExemplarId(),
                exemplar == null ? null : exemplar.getTombo(),
                solicitacao.getDataSolicitacao(),
                solicitacao.getDataAnalise(),
                analista == null ? null : analista.getNome(),
                solicitacao.getMotivoRecusa(),
                solicitacao.getDataLimiteRetirada(),
                solicitacao.getDataRetirada(),
                solicitacao.getRetiradaConfirmadaPor() == null ? null : solicitacao.getRetiradaConfirmadaPor().getNome(),
                solicitacao.getEmprestimo() == null ? null : solicitacao.getEmprestimo().getEmprestimoId(),
                solicitacao.getReserva() == null ? null : solicitacao.getReserva().getReservaId()
        );
    }
}
