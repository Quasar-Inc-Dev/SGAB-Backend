package sgab.sgab.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgab.sgab.events.ReservaDisponivelEvent;
import sgab.sgab.events.ReservaNaFilaEvent;
import sgab.sgab.repositories.ExemplarRepository;
import sgab.sgab.repositories.ReservaRepository;
import sgab.sgab.repositories.LeitorRepository;
import sgab.sgab.dtos.response.ReservaResponseDTO;
import sgab.sgab.entities.Exemplar;
import sgab.sgab.entities.Reserva;
import sgab.sgab.entities.Leitor;
import sgab.sgab.entities.Enum.StatusExemplar;
import sgab.sgab.entities.Enum.StatusReserva;
import sgab.sgab.exceptions.RecursoNaoEncontradoException;

@Service
public class ReservaService {

    // Prazo para o leitor retirar o exemplar separado para ele
    private static final int DIAS_PARA_RETIRADA = 3;

    private final ReservaRepository reservaRepository;
    private final LeitorRepository leitorRepository;
    private final ExemplarRepository exemplarRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReservaService(
            ReservaRepository reservaRepository,
            LeitorRepository leitorRepository,
            ExemplarRepository exemplarRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.reservaRepository = reservaRepository;
        this.leitorRepository = leitorRepository;
        this.exemplarRepository = exemplarRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Exemplar disponível: fica separado para o leitor na hora (AGUARDANDO_RETIRADA).
     * Exemplar emprestado ou já separado: o leitor entra na fila desse exemplar (ATIVA).
     */
    @Transactional
    public ReservaResponseDTO solicitarReserva(Integer exemplarId, Integer leitorId) {
        Exemplar exemplar = exemplarRepository.findById(exemplarId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Exemplar não encontrado para realizar a reserva."));

        Leitor leitor = leitorRepository.findById(leitorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Leitor não encontrado."));

        if ("INATIVO".equals(exemplar.getLivro().getLivroStatus())) {
            throw new IllegalArgumentException("O livro está desativado e não pode ser reservado.");
        }

        if (exemplar.getStatus() == StatusExemplar.MANUTENCAO || exemplar.getStatus() == StatusExemplar.EXTRAVIADO) {
            throw new IllegalArgumentException("Este exemplar não está em circulação e não pode ser reservado.");
        }

        boolean jaPossuiReserva = reservaRepository.existsByExemplarExemplarIdAndLeitorIdAndStatusIn(
                exemplarId, leitorId, List.of(StatusReserva.ATIVA, StatusReserva.AGUARDANDO_RETIRADA)
        );

        if (jaPossuiReserva) {
            throw new IllegalArgumentException("O leitor já possui uma reserva em andamento para este exemplar.");
        }

        Reserva reserva = new Reserva();
        reserva.setExemplar(exemplar);
        reserva.setLeitor(leitor);
        reserva.setDataReserva(LocalDateTime.now());

        if (exemplar.getStatus() == StatusExemplar.DISPONIVEL) {
            separarParaRetirada(reserva, exemplar);
        } else {
            reserva.setStatus(StatusReserva.ATIVA);
            eventPublisher.publishEvent(new ReservaNaFilaEvent(
                    leitor.getUsuario().getEmail(), leitor.getUsuario().getNome(), exemplar.getLivro().getTitulo()
            ));
        }

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

        boolean exemplarEstavaSeparado = reserva.getStatus() == StatusReserva.AGUARDANDO_RETIRADA;

        // A reserva é mantida com status CANCELADA porque faz parte do histórico
        reserva.setStatus(StatusReserva.CANCELADA);
        reserva.setDataLimiteRetirada(null);
        reservaRepository.save(reserva);

        // Se o exemplar estava separado para o leitor, ele vai para o próximo da fila
        if (exemplarEstavaSeparado) {
            destinarExemplarLiberado(reserva.getExemplar());
        }

        return converterParaResponse(reserva);
    }

    public boolean exemplarPossuiFilaDeEspera(Integer exemplarId) {
        return reservaRepository.existsByExemplarExemplarIdAndStatus(exemplarId, StatusReserva.ATIVA);
    }

    public Optional<Reserva> buscarReservaAguardandoRetirada(Integer livroId, Integer leitorId) {
        return reservaRepository.findFirstByExemplarLivroLivroIdAndLeitorIdAndStatus(
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
     * separa para o primeiro da fila desse exemplar ou deixa disponível.
     */
    @Transactional
    public void destinarExemplarLiberado(Exemplar exemplar) {
        Optional<Reserva> proximaDaFila = reservaRepository.findFirstByExemplarExemplarIdAndStatusOrderByDataReservaAsc(
                exemplar.getExemplarId(), StatusReserva.ATIVA
        );

        if (proximaDaFila.isPresent()) {
            Reserva reserva = proximaDaFila.get();
            separarParaRetirada(reserva, exemplar);
            reservaRepository.save(reserva);
        } else {
            exemplar.setStatus(StatusExemplar.DISPONIVEL);
        }

        exemplarRepository.save(exemplar);
    }

    // Avisa o leitor por e-mail: vale tanto para a reserva feita agora quanto para a vez dele na fila
    private void separarParaRetirada(Reserva reserva, Exemplar exemplar) {
        reserva.setStatus(StatusReserva.AGUARDANDO_RETIRADA);
        reserva.setDataLimiteRetirada(LocalDateTime.now().plusDays(DIAS_PARA_RETIRADA));
        exemplar.setStatus(StatusExemplar.RESERVADO);

        Leitor leitor = reserva.getLeitor();
        eventPublisher.publishEvent(new ReservaDisponivelEvent(
                leitor.getUsuario().getEmail(), leitor.getUsuario().getNome(),
                exemplar.getLivro().getTitulo(), reserva.getDataLimiteRetirada()
        ));
    }

    private Reserva buscarEntidadePorId(Integer id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva não encontrada."));
    }

    private ReservaResponseDTO converterParaResponse(Reserva reserva) {
        Exemplar exemplar = reserva.getExemplar();

        return new ReservaResponseDTO(
                reserva.getReservaId(),
                exemplar.getLivro().getLivroId(),
                exemplar.getLivro().getTitulo(),
                reserva.getLeitor().getId(),
                reserva.getLeitor().getUsuario().getNome(),
                reserva.getDataReserva(),
                reserva.getStatus(),
                exemplar.getExemplarId(),
                exemplar.getTombo(),
                reserva.getDataLimiteRetirada()
        );
    }
}
