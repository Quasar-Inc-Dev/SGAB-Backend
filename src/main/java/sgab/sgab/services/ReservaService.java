package sgab.sgab.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgab.sgab.repositories.ReservaRepository;
import sgab.sgab.repositories.ExemplarRepository;
import sgab.sgab.repositories.LeitorRepository;
import sgab.sgab.entities.Reserva;
import sgab.sgab.entities.Exemplar;
import sgab.sgab.entities.Leitor;
import sgab.sgab.dtos.response.ReservaResponseDTO;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ExemplarRepository exemplarRepository;
    private final LeitorRepository leitorRepository;

    public ReservaService(
            ReservaRepository reservaRepository,
            ExemplarRepository exemplarRepository,
            LeitorRepository leitorRepository
    ) {
        this.reservaRepository = reservaRepository;
        this.exemplarRepository = exemplarRepository;
        this.leitorRepository = leitorRepository;
    }

    @Transactional
    public ReservaResponseDTO solicitarReserva(Integer exemplarId, Integer leitorId) {
        Exemplar exemplar = exemplarRepository.findById(exemplarId)
                .orElseThrow(() -> new RuntimeException("Exemplar não encontrado para realizar a reserva."));

        Leitor leitor = leitorRepository.findById(leitorId)
                .orElseThrow(() -> new RuntimeException("Leitor não encontrado."));

        Reserva reserva = new Reserva();
        reserva.setExemplar(exemplar);
        reserva.setLeitor(leitor);
        // Se a sua entidade Reserva usar LocalDateTime para data_emprestimo/dataReserva, ajuste se necessário

        Reserva salva = reservaRepository.save(reserva);
        return converterParaDTO(salva);
    }

    public List<ReservaResponseDTO> listarTodas() {
        return reservaRepository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public ReservaResponseDTO buscarPorId(Integer id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada."));
        return converterParaDTO(reserva);
    }

    @Transactional
    public void cancelarReserva(Integer id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada."));
        reservaRepository.delete(reserva);
    }

private ReservaResponseDTO converterParaDTO(Reserva reserva) {
        Exemplar exemplar = reserva.getExemplar();
        Leitor leitor = reserva.getLeitor();

        return new ReservaResponseDTO(
                reserva.getReservaId(),
                exemplar != null && exemplar.getLivro() != null ? exemplar.getLivro().getLivroId() : null,
                exemplar != null && exemplar.getLivro() != null ? exemplar.getLivro().getTitulo() : null,
                leitor != null ? leitor.getId() : null,
                null,
                reserva.getData_emprestimo() != null ? reserva.getData_emprestimo().atStartOfDay() : null,
                null,
                exemplar != null ? exemplar.getExemplarId() : null,
                null,
                null
        );
    }
}
