package sgab.sgab.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sgab.sgab.entities.TokenRedefinicaoSenha;
import sgab.sgab.entities.Usuario;
import sgab.sgab.entities.valueobjects.Senha;
import sgab.sgab.events.RedefinicaoSenhaSolicitadaEvent;
import sgab.sgab.repositories.TokenRedefinicaoSenhaRepository;
import sgab.sgab.repositories.UsuarioRepository;

@Service
public class RedefinicaoSenhaService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final TokenRedefinicaoSenhaRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final long validadeMinutos;

    public RedefinicaoSenhaService(
            UsuarioRepository usuarioRepository,
            TokenRedefinicaoSenhaRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher,
            @Value("${sgab.redefinicao-senha.validade-minutos:30}") long validadeMinutos
    ) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.validadeMinutos = validadeMinutos;
    }

    /**
     * Gera o link de redefinição e dispara o e-mail.
     * Não avisa se o e-mail existe ou não, para ninguém usar a tela para descobrir quem tem conta.
     */
    @Transactional
    public void solicitar(String email) {
        usuarioRepository.findByEmail(email)
                .filter(Usuario::getStatusUsuario)
                .ifPresent(usuario -> {
                    tokenRepository.deleteByUsuarioIdAndDataUsoIsNull(usuario.getId());

                    String token = gerarToken();
                    LocalDateTime agora = LocalDateTime.now();

                    TokenRedefinicaoSenha registro = new TokenRedefinicaoSenha();
                    registro.setUsuario(usuario);
                    registro.setTokenHash(hash(token));
                    registro.setDataCriacao(agora);
                    registro.setDataExpiracao(agora.plusMinutes(validadeMinutos));
                    tokenRepository.save(registro);

                    eventPublisher.publishEvent(new RedefinicaoSenhaSolicitadaEvent(usuario.getEmail(), token));
                });
    }

    @Transactional
    public void redefinir(String token, String novaSenha) {
        TokenRedefinicaoSenha registro = tokenRepository.findByTokenHash(hash(token))
                .filter(t -> t.getDataUso() == null)
                .filter(t -> t.getDataExpiracao().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Link de redefinição inválido ou expirado. Solicite um novo."
                ));

        Usuario usuario = registro.getUsuario();
        usuario.setSenha(new Senha(passwordEncoder.encode(novaSenha)));

        registro.setDataUso(LocalDateTime.now());
    }

    // 32 bytes aleatórios em Base64 URL-safe: pode ir direto no link, sem escapar
    private String gerarToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
