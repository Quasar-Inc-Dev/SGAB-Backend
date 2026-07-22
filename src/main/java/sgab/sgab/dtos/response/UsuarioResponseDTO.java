package sgab.sgab.dtos.response;

public record UsuarioResponseDTO(
    String cpf,
    String nome,
    String email,
    Boolean statusUsuario
){}