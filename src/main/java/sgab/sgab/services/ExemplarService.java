package sgab.sgab.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sgab.sgab.dtos.response.ExemplarResponseDTO;
import sgab.sgab.entities.Exemplar;
import sgab.sgab.entities.Livro;
import sgab.sgab.entities.Enum.StatusExemplar;
import sgab.sgab.repositories.ExemplarRepository;

@Service
public class ExemplarService {

    private final ExemplarRepository exemplarRepository;

    public ExemplarService(ExemplarRepository exemplarRepository) {
        this.exemplarRepository = exemplarRepository;
    }

    @Transactional
    public List<ExemplarResponseDTO> adicionarExemplares(Livro livro, int quantidade) {
        if (quantidade < 1) {
            throw new IllegalArgumentException("A quantidade de exemplares deve ser maior que 0");
        }

        long sequencia = exemplarRepository.countByLivroLivroId(livro.getLivroId());
        List<Exemplar> novos = new ArrayList<>();

        for (int i = 0; i < quantidade; i++) {
            String tombo;
            do {
                sequencia++;
                tombo = gerarTombo(livro, sequencia);
            } while (exemplarRepository.existsByTombo(tombo));

            Exemplar exemplar = new Exemplar();
            exemplar.setLivro(livro);
            exemplar.setTombo(tombo);
            exemplar.setStatus(StatusExemplar.DISPONIVEL);
            novos.add(exemplar);
        }

        return exemplarRepository.saveAll(novos)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public List<ExemplarResponseDTO> listarPorLivro(Integer livroId) {
        return exemplarRepository.findByLivroLivroId(livroId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public long contarTotal(Integer livroId) {
        return exemplarRepository.countByLivroLivroId(livroId);
    }

    public long contarDisponiveis(Integer livroId) {
        return exemplarRepository.countByLivroLivroIdAndStatus(livroId, StatusExemplar.DISPONIVEL);
    }

    // Formato: L<id do livro>-<sequência>, ex.: L12-003
    private String gerarTombo(Livro livro, long sequencia) {
        return String.format("L%d-%03d", livro.getLivroId(), sequencia);
    }

    private ExemplarResponseDTO converterParaResponse(Exemplar exemplar) {
        return new ExemplarResponseDTO(
                exemplar.getExemplarId(),
                exemplar.getLivro().getLivroId(),
                exemplar.getTombo(),
                exemplar.getLocalizacao(),
                exemplar.getStatus(),
                exemplar.getDataAquisicao()
        );
    }
}
