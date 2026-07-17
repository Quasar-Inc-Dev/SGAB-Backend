package sgab.sgab.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Livro")
@Getter
@Setter
@NoArgsConstructor
public class Livro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "livro_id")
    private Integer livroId;

    @Column(nullable = false, length = 10)
    private String pha;

    @Column(nullable = false, length = 10)
    private String dewey;

    @Column(nullable = false, length = 20, unique = true)
    private String ISBN;

    @Column(nullable = false, length = 50)
    private String titulo;

    @Column(nullable = false, length = 100)
    private String subtitulo;

    @Column(length = 200)
    private String descricao;

    @Column(nullable = false, length = 100)
    private String autor;

    @Column(nullable = false, length = 100)
    private String editora;

    @Column(name = "livro_status", nullable = false, length = 50)
    private String livroStatus;

    @Column(nullable = false, length = 50)
    private String idioma;

    @Column(length = 255)
    private String area;

    @Column(nullable = false)
    private Integer paginas;

    private Integer ano;

    @Column(length = 100)
    private String genero;

    @Column(columnDefinition = "TEXT")
    private String tags;
}
