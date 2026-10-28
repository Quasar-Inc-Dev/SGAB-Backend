package sgab.sgab.dtos.request;

import jakarta.validation.constraints.NotBlank;
import sgab.sgab.entities.valueobjects.validation.cpf.ValidCpf;
import sgab.sgab.entities.valueobjects.validation.email.ValidEmail;
import sgab.sgab.entities.valueobjects.validation.senha.ValidSenha;

import java.time.LocalDate;

public record LeitorRequestDTO(
        @ValidCpf @NotBlank String cpf,
        @NotBlank String nome,
        @ValidEmail @NotBlank String email,
        @NotBlank @ValidSenha String senha,
        @NotBlank String genero,
        LocalDate dataNascimento,
        @NotBlank String tipoLeitor
) {
}
