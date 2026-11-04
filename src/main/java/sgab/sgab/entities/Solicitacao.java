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
@Table(name = "solicitacao", indexes = {
        // Fila do funcionário (por status, em ordem de chegada) e prazos de retirada vencidos
        @Index(name = "idx_solicitacao_status_data", columnList = "status, data_solicitacao"),
        @Index(name = "idx_solicitacao_status_limite", columnList = "status, data_limite_retirada"),
        // "Minhas solicitações" do leitor
        @Index(name = "idx_solicitacao_leitor_data", columnList = "leitor_id, data_solicitacao"),
        @Index(name = "idx_solicitacao_livro", columnList = "livro_id"),
        @Index(name = "idx_solicitacao_exemplar", columnList = "exemplar_id")
})
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leitor_id", nullable = false)
    private Leitor leitor;

    // Empréstimo é pedido pelo livro; reserva é pedida por um exemplar específico
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exemplar_id")
    private Exemplar exemplar;

    @Column(name = "data_solicitacao", nullable = false)
    private LocalDateTime dataSolicitacao = LocalDateTime.now();

    @Column(name = "data_analise")
    private LocalDateTime dataAnalise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analisado_por")
    private Usuario analisadoPor;

    @Column(name = "motivo_recusa", length = 255)
    private String motivoRecusa;

    // Retirada no balcão (só para EMPRESTIMO): o prazo do empréstimo começa aqui
    @Column(name = "data_limite_retirada")
    private LocalDateTime dataLimiteRetirada;

    @Column(name = "data_retirada")
    private LocalDateTime dataRetirada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retirada_confirmada_por")
    private Usuario retiradaConfirmadaPor;

    // Resultado da aprovação
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emprestimo_id")
    private Emprestimo emprestimo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;
}
