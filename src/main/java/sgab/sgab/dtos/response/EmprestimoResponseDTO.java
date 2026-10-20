package sgab.sgab.dtos.response;

import java.time.LocalDateTime;

public record EmprestimoResponseDTO(
        Integer emprestimoId,
        Integer exemplarId,
        String tombo,
        Integer livroId,
        String titulo,
        Integer leitorId,
        String nomeLeitor,
        LocalDateTime dataEmprestimo,
        LocalDateTime dataDevolucaoPrevista,
        LocalDateTime dataDevolucao,
        Boolean statusDevolucao
) {
}
