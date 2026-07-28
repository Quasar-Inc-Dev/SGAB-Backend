package sgab.sgab.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "funcionario")
@Getter
@Setter
@NoArgsConstructor
public class Funcionario {

    @Id
    @Column(length = 11)
    private String cpf;

    @OneToOne
    @MapsId
    @JoinColumn(name = "cpf")
    private Usuario usuario;

    @Column(nullable = false, length = 20, unique = true)
    private String matricula;

    @Column(name = "data_contratacao", nullable = false)
    private LocalDate dataContratacao;

    @Column(name = "status_funcionario", nullable = false)
    private Boolean statusFuncionario = true;
}