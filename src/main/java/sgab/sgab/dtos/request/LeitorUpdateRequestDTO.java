package sgab.sgab.dtos.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LeitorUpdateRequestDTO (
    @NotBlank String nome,
    @NotBlank String genero,
    @NotNull LocalDate dataNascimento,
    @NotBlank String tipoLeitor
) {}
