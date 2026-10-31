package sgab.sgab.events;

import java.time.LocalDateTime;

/**
 * Publicado quando a solicitação de empréstimo é aprovada e um exemplar fica separado para o leitor.
 * O prazo de devolução só começa na retirada, então o aviso leva o prazo para retirar.
 */
public record EmprestimoAprovadoEvent(String email, String nome, String tituloLivro, LocalDateTime dataLimiteRetirada) {
}
