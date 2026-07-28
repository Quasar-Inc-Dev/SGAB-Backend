package sgab.sgab.dtos.response;

import java.time.LocalDate;

public record LeitorResponseDTO (
        String cpf,
        String nome,
        String email,
        String genero,
        LocalDate dataNascimento,
        String tipoLeitor,
        Boolean statusLeitor
){}
