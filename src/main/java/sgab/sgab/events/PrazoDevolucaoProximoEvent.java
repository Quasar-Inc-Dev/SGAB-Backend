package sgab.sgab.events;

import java.time.LocalDateTime;

/**
 * Publicado pelo lembrete agendado quando a devolução de um empréstimo está perto de vencer.
 */
public record PrazoDevolucaoProximoEvent(String email, String nome, String tituloLivro, LocalDateTime dataDevolucaoPrevista) {
}
