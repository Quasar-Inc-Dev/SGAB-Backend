package sgab.sgab.entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgab.sgab.entities.Enum.StatusSolicitacao;
import sgab.sgab.entities.Enum.TipoSolicitacao;

/**
 * Pedido feito pelo leitor que precisa ser aprovado por um funcionário.
 * Só quando aprovado vira um Emprestimo ou uma Reserva de fato.
 */
@Entity
@Table(name = "solicitacao")
@Getter
@Setter
@NoArgsConstructor
public class Solicitacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "solicitacao_id")
    private Integer solicitacaoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoSolicitacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSolicitacao status = StatusSolicitacao.PENDENTE;

    @ManyToOne
    @JoinColumn(name = "leitor_id", nullable = false)
    private Leitor leitor;

    // Empréstimo é pedido pelo livro; reserva é pedida por um exemplar específico
    @ManyToOne
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @ManyToOne
    @JoinColumn(name = "exemplar_id")
    private Exemplar exemplar;

    @Column(name = "data_solicitacao", nullable = false)
    private LocalDateTime dataSolicitacao = LocalDateTime.now();

    @Column(name = "data_analise")
    private LocalDateTime dataAnalise;

    @ManyToOne
    @JoinColumn(name = "analisado_por")
    private Usuario analisadoPor;

    @Column(name = "motivo_recusa", length = 255)
    private String motivoRecusa;

    // Retirada no balcão (só para EMPRESTIMO): o prazo do empréstimo começa aqui
    @Column(name = "data_limite_retirada")
    private LocalDateTime dataLimiteRetirada;

    @Column(name = "data_retirada")
    private LocalDateTime dataRetirada;

    @ManyToOne
    @JoinColumn(name = "retirada_confirmada_por")
    private Usuario retiradaConfirmadaPor;

    // Resultado da aprovação
    @OneToOne
    @JoinColumn(name = "emprestimo_id")
    private Emprestimo emprestimo;

    @OneToOne
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;
}
