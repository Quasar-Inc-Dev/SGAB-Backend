package sgab.sgab.events;

import sgab.sgab.entities.Enum.TipoSolicitacao;

/**
 * Publicado quando uma solicitação de empréstimo ou de reserva é recusada.
 */
public record SolicitacaoRecusadaEvent(String email, String nome, String tituloLivro, TipoSolicitacao tipo, String motivo) {
}
