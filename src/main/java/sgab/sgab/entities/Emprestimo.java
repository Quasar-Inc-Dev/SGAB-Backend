package sgab.sgab.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "emprestimo")
@Getter
@Setter
@NoArgsConstructor
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emprestimo_id")
    private Integer emprestimoId;

    @ManyToOne
    @JoinColumn(name = "exemplar_id", nullable = false)
    private Exemplar exemplar;

    @ManyToOne
    @JoinColumn(name = "leitor_id", nullable = false)
    private Leitor leitor;

    @Column(name = "data_emprestimo", nullable = false)
    private LocalDateTime dataEmprestimo = LocalDateTime.now();

    @Column(name = "data_devolucao_prevista", nullable = false)
    private LocalDateTime dataDevolucaoPrevista;

    @Column(name = "data_devolucao")
    private LocalDateTime dataDevolucao;

    @Column(name = "status_devolucao", nullable = false)
    private Boolean statusDevolucao = false;
}