package sgab.sgab.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record UsuarioRequestDTO(
    @NotBlank String cpf
) {}
