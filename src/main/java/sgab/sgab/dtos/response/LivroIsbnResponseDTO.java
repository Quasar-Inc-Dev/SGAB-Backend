package sgab.sgab.dtos.response;

public record LivroIsbnResponseDTO(
        String isbn,
        String titulo,
        String subtitulo,
        String descricao,
        String autor,
        String editora,
        String idioma,
        Integer paginas,
        Integer ano,
        String genero
) {
}
