package sgab.sgab.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "administrador")
@Getter
@Setter
@NoArgsConstructor
public class Administrador {

    
    @Id
    private Integer id;

    @Column(length = 11)
    private String cpf;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    @Column(name = "status_administrador", nullable = false)
    private Boolean statusAdministrador = true;
}