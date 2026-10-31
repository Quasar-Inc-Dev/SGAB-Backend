package sgab.sgab.events;

/**
 * Publicado quando a reserva é registrada mas o exemplar está ocupado, e o leitor entra na fila.
 */
public record ReservaNaFilaEvent(String email, String nome, String tituloLivro) {
}
