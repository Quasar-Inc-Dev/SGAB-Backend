package sgab.sgab.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "exemplar")
@Getter
@Setter
@NoArgsConstructor
public class Exemplar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exemplar_id")
    private Integer exemplarId;

    @ManyToOne
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
}
