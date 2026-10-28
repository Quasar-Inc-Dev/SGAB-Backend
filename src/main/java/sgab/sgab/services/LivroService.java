package sgab.sgab.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sgab.sgab.repositories.EmprestimoRepository;
import sgab.sgab.repositories.ExemplarRepository;
import sgab.sgab.repositories.LivroRepository;
import sgab.sgab.repositories.ReservaRepository;
import sgab.sgab.repositories.SolicitacaoRepository;
import sgab.sgab.dtos.request.LivroRequestDTO;
import sgab.sgab.dtos.response.ExemplarResponseDTO;
import sgab.sgab.dtos.response.LivroResponseDTO;
import sgab.sgab.entities.Livro;
import sgab.sgab.exceptions.LivroNaoEncontradoException;

@Service
public class LivroService {

    private final LivroRepository livroRepository;
    private final ExemplarRepository exemplarRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final ReservaRepository reservaRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final ExemplarService exemplarService;

    public LivroService(
            LivroRepository livroRepository,
            ExemplarRepository exemplarRepository,
            EmprestimoRepository emprestimoRepository,
            ReservaRepository reservaRepository,
            SolicitacaoRepository solicitacaoRepository,
            ExemplarService exemplarService
    ) {
        this.livroRepository = livroRepository;
        this.exemplarRepository = exemplarRepository;
        this.emprestimoRepository = emprestimoRepository;
        this.reservaRepository = reservaRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.exemplarService = exemplarService;
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

        int quantidade = dto.quantidadeExemplares() == null ? 1 : dto.quantidadeExemplares();
        exemplarService.adicionarExemplares(salvo, quantidade);

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
    public LivroResponseDTO desativar(Integer id) {
        Livro livro = buscarEntidadePorId(id);

        if ("INATIVO".equals(livro.getLivroStatus())) {
            throw new IllegalArgumentException(
                    "O livro já está desativado"
            );
        }

        livro.setLivroStatus("INATIVO");

        Livro desativado = livroRepository.save(livro);

        return converterParaResponse(desativado);
    }

    @Transactional
    public void excluir(Integer id) {
        Livro livro = buscarEntidadePorId(id);

        // Empréstimos e reservas fazem parte do histórico; nesse caso o livro deve ser desativado
        if (emprestimoRepository.existsByExemplarLivroLivroId(id) || reservaRepository.existsByExemplarLivroLivroId(id)
                || solicitacaoRepository.existsByLivroLivroId(id)) {
            throw new IllegalArgumentException(
                    "O livro possui empréstimos ou reservas registrados e não pode ser excluído. Desative-o."
            );
        }

        exemplarRepository.deleteAll(exemplarRepository.findByLivroLivroId(id));
        livroRepository.delete(livro);
    }

    @Transactional
    public List<ExemplarResponseDTO> adicionarExemplares(Integer livroId, int quantidade) {
        Livro livro = buscarEntidadePorId(livroId);
        return exemplarService.adicionarExemplares(livro, quantidade);
    }

    public List<ExemplarResponseDTO> listarExemplares(Integer livroId) {
        buscarEntidadePorId(livroId);
        return exemplarService.listarPorLivro(livroId);
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
        livro.setImgUrl(dto.imgUrl());
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
                livro.getLivroStatus(),
                livro.getImgUrl(),
                exemplarService.contarTotal(livro.getLivroId()),
                exemplarService.contarDisponiveis(livro.getLivroId())
        );
    }

    private String normalizarIsbn(String isbn) {
        return isbn.replaceAll("[^0-9Xx]", "").toUpperCase();
    }
}
