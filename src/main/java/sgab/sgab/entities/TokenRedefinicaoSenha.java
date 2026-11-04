package sgab.sgab.entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Token do "esqueci minha senha".
 * Só o hash SHA-256 é gravado: quem ler o banco não consegue usar o link.
 */
@Entity
@Table(name = "token_redefinicao_senha",
        indexes = @Index(name = "idx_token_redefinicao_usuario", columnList = "usuario_id"))
@Getter
@Setter
@NoArgsConstructor
public class TokenRedefinicaoSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Integer tokenId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    // Preenchida quando o token é usado; depois disso ele não vale mais
    @Column(name = "data_uso")
    private LocalDateTime dataUso;
}
