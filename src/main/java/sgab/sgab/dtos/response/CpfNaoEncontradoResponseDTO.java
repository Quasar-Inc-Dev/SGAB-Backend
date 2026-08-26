package sgab.sgab.dtos.response;

import sgab.sgab.entities.Enum.TipoUsuario;

public record CpfNaoEncontradoResponseDTO(
    Integer id,
    String cpf,
    String nome,
    String email,
    TipoUsuario tipoUsuario,
    Boolean statusUsuario
) {}
