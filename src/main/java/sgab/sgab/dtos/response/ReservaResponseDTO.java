package sgab.sgab.dtos.response;

import java.time.LocalDateTime;

import sgab.sgab.entities.Enum.StatusReserva;

public record ReservaResponseDTO(
        Integer reservaId,
        Integer livroId,
        String titulo,
        Integer leitorId,
        String nomeLeitor,
        LocalDateTime dataReserva,
        StatusReserva status,
        Integer exemplarId,
        String tombo,
        LocalDateTime dataLimiteRetirada
) {
}
