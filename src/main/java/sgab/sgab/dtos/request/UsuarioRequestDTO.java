package sgab.sgab.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import sgab.sgab.entities.Enum.TipoUsuario;
import sgab.sgab.entities.valueobjects.validation.cpf.ValidCpf;
import sgab.sgab.entities.valueobjects.validation.email.ValidEmail;

public record UsuarioRequestDTO (
    @ValidCpf
    @NotBlank
    String cpf, 

    @NotBlank
    String nome,

    @ValidEmail
    @NotBlank
    String email,

    @NotBlank
    String senha,

    @NotNull
    TipoUsuario tipoUsuario
) {}
