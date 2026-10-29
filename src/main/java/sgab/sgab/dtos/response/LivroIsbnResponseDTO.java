package sgab.sgab.dtos.response;

public record LivroIsbnResponseDTO(
        String isbn,
        String titulo,
        String subtitulo,
        String descricao,
        String autores,
        String editora,
        String idioma,
        Integer numeroPaginas,
        Integer anoPublicacao,
        String categorias,
        String urlCapa
) {}
