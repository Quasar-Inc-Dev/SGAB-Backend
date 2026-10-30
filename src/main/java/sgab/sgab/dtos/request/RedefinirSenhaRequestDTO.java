package sgab.sgab.dtos.request;

import jakarta.validation.constraints.NotBlank;
import sgab.sgab.entities.valueobjects.validation.senha.ValidSenha;

public record RedefinirSenhaRequestDTO(
        @NotBlank String token,
        @NotBlank @ValidSenha String novaSenha
) {
}
