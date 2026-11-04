package sgab.sgab.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "emprestimo", indexes = {
        @Index(name = "idx_emprestimo_exemplar", columnList = "exemplar_id"),
        @Index(name = "idx_emprestimo_leitor", columnList = "leitor_id"),
        // Busca do agendador de lembretes: empréstimos em aberto por data de vencimento
        @Index(name = "idx_emprestimo_aberto_vencimento", columnList = "status_devolucao, data_devolucao_prevista")
})
@Getter
@Setter
@NoArgsConstructor
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emprestimo_id")
    private Integer emprestimoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exemplar_id", nullable = false)
    private Exemplar exemplar;

    @ManyToOne(fetch = FetchType.LAZY)
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

    // Evita reenviar o lembrete de devolução; volta a false quando o empréstimo é renovado
    @Column(name = "lembrete_prazo_enviado")
    private Boolean lembretePrazoEnviado = false;
}
