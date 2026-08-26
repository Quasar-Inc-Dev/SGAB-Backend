package sgab.sgab.dtos.response;

public record AdministradorResponseDTO (
    Integer id,
    String cpf,
    String nome,
    String email,
    Boolean statusAdministrador
){}
