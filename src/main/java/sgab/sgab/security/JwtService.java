package sgab.sgab.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;

@Service 
public class JwtService {
    private final SecretKey secretKey = Jwts.SIG.HS256.key().build(); //secret key salva em memoria, quando for para produção mudar para uma configuração fixa
    private static final long EXPIRATION_MS = 1000 * 60 * 60; //1 hora
    
    public String gerarToken(UsuarioDetails usuarioDetails) {
        return Jwts.builder()
            .subject(usuarioDetails.getUsername())
            .claim("tipoUsuario", usuarioDetails.getUsuario().getTipoUsuario().name())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
            .signWith((secretKey))
            .compact();
    }
}
