package sgab.sgab.dtos.response;

import java.time.LocalDateTime;

import sgab.sgab.entities.Enum.StatusExemplar;

public record ExemplarResponseDTO(
        Integer exemplarId,
        Integer livroId,
        String tombo,
        String localizacao,
        StatusExemplar status,
        LocalDateTime dataAquisicao
) {
}
