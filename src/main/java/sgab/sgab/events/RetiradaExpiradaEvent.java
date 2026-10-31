package sgab.sgab.events;

/**
 * Publicado quando o leitor não retira no prazo o exemplar separado para o empréstimo.
 */
public record RetiradaExpiradaEvent(String email, String nome, String tituloLivro) {
}
