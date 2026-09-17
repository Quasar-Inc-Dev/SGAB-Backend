package sgab.sgab.Services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sgab.sgab.Repositories.LivroRepository;
import sgab.sgab.dtos.request.LivroRequestDTO;
import sgab.sgab.dtos.response.LivroResponseDTO;
import sgab.sgab.entities.Livro;
import sgab.sgab.exceptions.LivroNaoEncontradoException;

@Service
public class LivroService {

    private final LivroRepository livroRepository;

    public LivroService(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    @Transactional
    public LivroResponseDTO cadastrar(LivroRequestDTO dto) {
        String isbn = normalizarIsbn(dto.isbn());

        if (livroRepository.existsByISBN(isbn)) {
            throw new IllegalArgumentException(
                    "Já existe um livro cadastrado com este ISBN"
            );
        }

        Livro livro = new Livro();
        preencherLivro(livro, dto, isbn);

        Livro salvo = livroRepository.save(livro);

        return converterParaResponse(salvo);
    }

    public List<LivroResponseDTO> listarTodos() {
        return livroRepository.findAll()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public LivroResponseDTO buscarPorId(Integer id) {
        Livro livro = buscarEntidadePorId(id);
        return converterParaResponse(livro);
    }

    public LivroResponseDTO buscarPorIsbn(String isbn) {
        Livro livro = livroRepository.findByISBN(normalizarIsbn(isbn))
                .orElseThrow(() -> new LivroNaoEncontradoException(
                        "Livro não encontrado para o ISBN informado"
                ));

        return converterParaResponse(livro);
    }

    @Transactional
    public LivroResponseDTO atualizar(Integer id, LivroRequestDTO dto) {
        Livro livro = buscarEntidadePorId(id);
        String isbn = normalizarIsbn(dto.isbn());

        boolean isbnPertenceAOutroLivro = livroRepository.findByISBN(isbn)
                .filter(outroLivro -> !outroLivro.getLivroId().equals(id))
                .isPresent();

        if (isbnPertenceAOutroLivro) {
            throw new IllegalArgumentException(
                    "O ISBN informado já pertence a outro livro"
            );
        }

        preencherLivro(livro, dto, isbn);

        Livro atualizado = livroRepository.save(livro);

        return converterParaResponse(atualizado);
    }

    @Transactional
    public void excluir(Integer id) {
        Livro livro = buscarEntidadePorId(id);
        livroRepository.delete(livro);
    }

    private Livro buscarEntidadePorId(Integer id) {
        return livroRepository.findById(id)
                .orElseThrow(() -> new LivroNaoEncontradoException(
                        "Livro não encontrado"
                ));
    }

    private void preencherLivro(
            Livro livro,
            LivroRequestDTO dto,
            String isbn
    ) {
        livro.setISBN(isbn);
        livro.setTitulo(dto.titulo());
        livro.setSubtitulo(dto.subtitulo());
        livro.setDescricao(dto.descricao());
        livro.setAutor(dto.autor());
        livro.setEditora(dto.editora());
        livro.setIdioma(dto.idioma());
        livro.setPaginas(dto.paginas());
        livro.setAno(dto.ano());
        livro.setGenero(dto.genero());
        livro.setTags(dto.tags());
        livro.setPha(dto.pha());
        livro.setDewey(dto.dewey());
        livro.setArea(dto.area());
        livro.setLivroStatus(dto.livroStatus());
    }

    private LivroResponseDTO converterParaResponse(Livro livro) {
        return new LivroResponseDTO(
                livro.getLivroId(),
                livro.getISBN(),
                livro.getTitulo(),
                livro.getSubtitulo(),
                livro.getDescricao(),
                livro.getAutor(),
                livro.getEditora(),
                livro.getIdioma(),
                livro.getPaginas(),
                livro.getAno(),
                livro.getGenero(),
                livro.getTags(),
                livro.getPha(),
                livro.getDewey(),
                livro.getArea(),
                livro.getLivroStatus()
        );
    }

    private String normalizarIsbn(String isbn) {
        return isbn.replaceAll("[^0-9Xx]", "").toUpperCase();
    }
}
