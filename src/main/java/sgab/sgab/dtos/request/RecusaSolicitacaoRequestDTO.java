package sgab.sgab.dtos.request;

import jakarta.validation.constraints.Size;

public record RecusaSolicitacaoRequestDTO(
        @Size(max = 255)
        String motivo
) {
}
