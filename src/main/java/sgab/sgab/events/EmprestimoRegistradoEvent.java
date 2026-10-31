package sgab.sgab.events;

import java.time.LocalDateTime;

/**
 * Publicado quando o empréstimo é criado (retirada do exemplar no balcão).
 */
public record EmprestimoRegistradoEvent(String email, String nome, String tituloLivro, LocalDateTime dataDevolucaoPrevista) {
}
