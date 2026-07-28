package sgab.sgab.dtos.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import sgab.sgab.entities.valueobjects.validation.cpf.ValidCpf;
import sgab.sgab.entities.valueobjects.validation.email.ValidEmail;

public record FuncionarioRequestDTO(
        @ValidCpf @NotBlank String cpf,
        @NotBlank String nome,
        @ValidEmail @NotBlank String email,
        @NotBlank String senha,
        @NotBlank String matricula,
        LocalDate dataContratacao
) {
}