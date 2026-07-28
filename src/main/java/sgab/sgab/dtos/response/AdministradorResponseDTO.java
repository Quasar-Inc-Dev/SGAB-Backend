package sgab.sgab.dtos.response;

public record AdministradorResponseDTO (
    String cpf,
    String nome,
    String email,
    Boolean statusAdministrador
){}
