package sgab.sgab.dtos.response;

import java.time.LocalDate;

public record FuncionarioResponseDTO(
        Integer id,
        String cpf,
        String nome,
        String email,
        String matricula,
        LocalDate dataContratacao,
        Boolean statusFuncionario
){}