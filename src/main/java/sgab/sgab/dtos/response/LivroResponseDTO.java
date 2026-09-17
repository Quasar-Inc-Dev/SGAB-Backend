package sgab.sgab.dtos.response;

public record LivroResponseDTO(
        Integer livroId,
        String isbn,
        String titulo,
        String subtitulo,
        String descricao,
        String autor,
        String editora,
        String idioma,
        Integer paginas,
        Integer ano,
        String genero,
        String tags,
        String pha,
        String dewey,
        String area,
        String livroStatus
) {
}
