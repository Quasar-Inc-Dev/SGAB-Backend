package sgab.sgab.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgab.sgab.repositories.ExemplarRepository;
import sgab.sgab.repositories.ReservaRepository;
import sgab.sgab.repositories.LivroRepository;
import sgab.sgab.repositories.LeitorRepository;
import sgab.sgab.dtos.response.ReservaResponseDTO;
import sgab.sgab.entities.Exemplar;
import sgab.sgab.entities.Reserva;
import sgab.sgab.entities.Livro;
import sgab.sgab.entities.Leitor;
import sgab.sgab.entities.Enum.StatusExemplar;
import sgab.sgab.entities.Enum.StatusReserva;
import sgab.sgab.exceptions.LivroNaoEncontradoException;
import sgab.sgab.exceptions.RecursoNaoEncontradoException;

@Service
public class ReservaService {

    // Prazo para o leitor retirar o exemplar separado para ele
    private static final int DIAS_PARA_RETIRADA = 3;

    private final ReservaRepository reservaRepository;
    private final LivroRepository livroRepository;
    private final LeitorRepository leitorRepository;
    private final ExemplarRepository exemplarRepository;

    public ReservaService(
            ReservaRepository reservaRepository,
            LivroRepository livroRepository,
            LeitorRepository leitorRepository,
            ExemplarRepository exemplarRepository
    ) {
        this.reservaRepository = reservaRepository;
        this.livroRepository = livroRepository;
        this.leitorRepository = leitorRepository;
        this.exemplarRepository = exemplarRepository;
    }

    @Transactional
    public ReservaResponseDTO solicitarReserva(Integer livroId, Integer leitorId) {
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new LivroNaoEncontradoException("Livro não encontrado para realizar a reserva."));

        Leitor leitor = leitorRepository.findById(leitorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Leitor não encontrado."));

        if ("INATIVO".equals(livro.getLivroStatus())) {
            throw new IllegalArgumentException("O livro está desativado e não pode ser reservado.");
        }

        if (exemplarRepository.countByLivroLivroIdAndStatus(livroId, StatusExemplar.DISPONIVEL) > 0) {
            throw new IllegalArgumentException("Há exemplares disponíveis deste livro. Faça o empréstimo diretamente.");
        }

        boolean jaPossuiReserva = reservaRepository.existsByLivroLivroIdAndLeitorIdAndStatusIn(
                livroId, leitorId, List.of(StatusReserva.ATIVA, StatusReserva.AGUARDANDO_RETIRADA)
        );

        if (jaPossuiReserva) {
            throw new IllegalArgumentException("O leitor já possui uma reserva em andamento para este livro.");
        }

        Reserva reserva = new Reserva();
        reserva.setLivro(livro);
        reserva.setLeitor(leitor);
        reserva.setDataReserva(LocalDateTime.now());
        reserva.setStatus(StatusReserva.ATIVA);

        return converterParaResponse(reservaRepository.save(reserva));
    }

    public List<ReservaResponseDTO> listarTodas() {
        return reservaRepository.findAll()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public ReservaResponseDTO buscarPorId(Integer id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    public ReservaResponseDTO cancelarReserva(Integer id) {
        Reserva reserva = buscarEntidadePorId(id);

        if (reserva.getStatus() != StatusReserva.ATIVA && reserva.getStatus() != StatusReserva.AGUARDANDO_RETIRADA) {
            throw new IllegalArgumentException("Só é possível cancelar reservas em andamento.");
        }

        Exemplar exemplarSeparado = reserva.getExemplar();

        // A reserva é mantida com status CANCELADA porque faz parte do histórico
        reserva.setStatus(StatusReserva.CANCELADA);
        reserva.setExemplar(null);
        reserva.setDataLimiteRetirada(null);
        reservaRepository.save(reserva);

        // Se havia um exemplar separado para o leitor, ele vai para o próximo da fila
        if (exemplarSeparado != null) {
            destinarExemplarLiberado(exemplarSeparado);
        }

        return converterParaResponse(reserva);
    }

    public boolean livroPossuiFilaDeEspera(Integer livroId) {
        return reservaRepository.existsByLivroLivroIdAndStatus(livroId, StatusReserva.ATIVA);
    }

    public Optional<Reserva> buscarReservaAguardandoRetirada(Integer livroId, Integer leitorId) {
        return reservaRepository.findFirstByLivroLivroIdAndLeitorIdAndStatus(
                livroId, leitorId, StatusReserva.AGUARDANDO_RETIRADA
        );
    }

    @Transactional
    public void marcarComoAtendida(Reserva reserva) {
        reserva.setStatus(StatusReserva.ATENDIDA);
        reservaRepository.save(reserva);
    }

    /**
     * Decide o destino de um exemplar que voltou para a biblioteca:
     * separa para o primeiro da fila de reservas ou deixa disponível.
     */
    @Transactional
    public void destinarExemplarLiberado(Exemplar exemplar) {
        Optional<Reserva> proximaDaFila = reservaRepository.findFirstByLivroLivroIdAndStatusOrderByDataReservaAsc(
                exemplar.getLivro().getLivroId(), StatusReserva.ATIVA
        );

        if (proximaDaFila.isPresent()) {
            Reserva reserva = proximaDaFila.get();
            reserva.setStatus(StatusReserva.AGUARDANDO_RETIRADA);
            reserva.setExemplar(exemplar);
            reserva.setDataLimiteRetirada(LocalDateTime.now().plusDays(DIAS_PARA_RETIRADA));
            reservaRepository.save(reserva);

            exemplar.setStatus(StatusExemplar.RESERVADO);
        } else {
            exemplar.setStatus(StatusExemplar.DISPONIVEL);
        }

        exemplarRepository.save(exemplar);
    }

    private Reserva buscarEntidadePorId(Integer id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva não encontrada."));
    }

    private ReservaResponseDTO converterParaResponse(Reserva reserva) {
        Exemplar exemplar = reserva.getExemplar();

        return new ReservaResponseDTO(
                reserva.getReservaId(),
                reserva.getLivro().getLivroId(),
                reserva.getLivro().getTitulo(),
                reserva.getLeitor().getId(),
                reserva.getLeitor().getUsuario().getNome(),
                reserva.getDataReserva(),
                reserva.getStatus(),
                exemplar == null ? null : exemplar.getExemplarId(),
                exemplar == null ? null : exemplar.getTombo(),
                reserva.getDataLimiteRetirada()
        );
    }
}
