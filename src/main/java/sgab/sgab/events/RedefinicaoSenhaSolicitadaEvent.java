package sgab.sgab.events;

/**
 * Publicado quando o usuário pede para redefinir a senha.
 * O token vai em texto puro só aqui, para montar o link do e-mail; no banco fica apenas o hash.
 */
public record RedefinicaoSenhaSolicitadaEvent(String email, String token) {
}
