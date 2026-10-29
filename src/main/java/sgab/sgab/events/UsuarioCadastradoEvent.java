package sgab.sgab.events;

/**
 * Publicado quando um usuário novo é cadastrado.
 * Carrega só os dados necessários para a notificação, sem a entidade JPA,
 * porque o listener roda em outra thread, fora da transação.
 */
public record UsuarioCadastradoEvent(String email, String nome) {
}
