package sgab.sgab.entities;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Leitor")
@Getter
@Setter
@NoArgsConstructor
public class Leitor {
    @Id
    @Column(length = 11)
    private String cpf;

    @OneToOne
    @MapsId
    @JoinColumn(name = "cpf")
    private Usuario usuario;

    @Column(nullable = false, length = 50)
    private String genero;

    @Column(nullable = false, name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "tipo_leitor", nullable = false, length = 50)
    private String tipoLeitor;

    @Column(name = "status_leitor", nullable = false)
    private Boolean statusLeitor = true;
}
