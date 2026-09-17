package sgab.sgab.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LivroRequestDTO(

        @NotBlank
        String isbn,

        @NotBlank
        String titulo,

        String subtitulo,

        String descricao,

        @NotBlank
        String autor,

        @NotBlank
        String editora,

        @NotBlank
        String idioma,

        @NotNull
        @Positive
        Integer paginas,

        Integer ano,

        String genero,

        String tags,

        @NotBlank
        String pha,

        @NotBlank
        String dewey,

        String area,

        @NotBlank
        String livroStatus
) {
}
