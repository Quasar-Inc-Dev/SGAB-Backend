package sgab.sgab.dtos.response;

import sgab.sgab.entities.Enum.TipoUsuario;

public record UsuarioResponseDTO(
    Integer id,
    String cpf,
    String nome,
    String email,
    TipoUsuario tipoUsuario,
    Boolean statusUsuario
) {}
