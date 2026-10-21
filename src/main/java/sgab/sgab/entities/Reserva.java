package sgab.sgab.entities;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgab.sgab.entities.Enum.StatusReserva;

@Entity
@Table(name = "reserva")
@Getter
@Setter
@NoArgsConstructor
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reserva_id")
    private Integer reservaId;

    // A reserva é de um exemplar específico; cada exemplar tem sua própria fila
    @ManyToOne
    @JoinColumn(name = "exemplar_id", nullable = false)
    private Exemplar exemplar;

    @ManyToOne
    @JoinColumn(name = "leitor_id", nullable = false)
    private Leitor leitor;

    @Column(name = "data_reserva", nullable = false)
    private LocalDateTime dataReserva = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusReserva status = StatusReserva.ATIVA;

    // Preenchido quando o exemplar fica separado para o leitor (status AGUARDANDO_RETIRADA)
    @Column(name = "data_limite_retirada")
    private LocalDateTime dataLimiteRetirada;
}
