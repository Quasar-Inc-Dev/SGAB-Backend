package sgab.sgab.events;

import java.time.LocalDateTime;

/**
 * Publicado quando o exemplar reservado fica separado para o leitor (status AGUARDANDO_RETIRADA),
 * seja na hora da reserva ou quando chega a vez dele na fila.
 */
public record ReservaDisponivelEvent(String email, String nome, String tituloLivro, LocalDateTime dataLimiteRetirada) {
}
