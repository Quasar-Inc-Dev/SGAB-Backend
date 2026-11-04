package sgab.sgab.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgab.sgab.entities.Enum.StatusExemplar;

import java.time.LocalDateTime;

@Entity
// O Postgres não indexa chaves estrangeiras sozinho; livro_id + status cobre contagens e "primeiro disponível"
@Table(name = "exemplar", indexes = @Index(name = "idx_exemplar_livro_status", columnList = "livro_id, status"))
@Getter
@Setter
@NoArgsConstructor
public class Exemplar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exemplar_id")
    private Integer exemplarId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @Column(nullable = false, length = 20, unique = true)
    private String tombo;

    @Column(length = 100)
    private String localizacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusExemplar status = StatusExemplar.DISPONIVEL;

    @Column(name = "data_aquisicao", nullable = false)
    private LocalDateTime dataAquisicao = LocalDateTime.now();

    // Lock otimista: impede que duas transações emprestem o mesmo exemplar ao mesmo tempo
    @Version
    private Long versao;
}
