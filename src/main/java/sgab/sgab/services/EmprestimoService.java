package sgab.sgab.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgab.sgab.repositories.EmprestimoRepository;
import sgab.sgab.repositories.ExemplarRepository;
import sgab.sgab.repositories.LeitorRepository;
import sgab.sgab.entities.Emprestimo;
import sgab.sgab.entities.Exemplar;
import sgab.sgab.entities.Leitor;
import sgab.sgab.dtos.response.EmprestimoResponseDTO;

@Service
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final ExemplarRepository exemplarRepository;
    private final LeitorRepository leitorRepository;

    public EmprestimoService(
            EmprestimoRepository emprestimoRepository,
            ExemplarRepository exemplarRepository,
            LeitorRepository leitorRepository
    ) {
        this.emprestimoRepository = emprestimoRepository;
        this.exemplarRepository = exemplarRepository;
        this.leitorRepository = leitorRepository;
    }

    public List<EmprestimoResponseDTO> listarTodos() {
        return emprestimoRepository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public EmprestimoResponseDTO buscarPorId(Integer id) {
        Emprestimo emprestimo = emprestimoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empréstimo não encontrado."));
        return converterParaDTO(emprestimo);
    }

    @Transactional
    public EmprestimoResponseDTO registrarEmprestimo(Integer exemplarId, Integer leitorId) {
        Exemplar exemplar = exemplarRepository.findById(exemplarId)
                .orElseThrow(() -> new RuntimeException("Exemplar não encontrado."));

        Leitor leitor = leitorRepository.findById(leitorId)
                .orElseThrow(() -> new RuntimeException("Leitor não encontrado."));

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setExemplar(exemplar);
        emprestimo.setLeitor(leitor);

        LocalDateTime dataEmprestimo = LocalDateTime.now();
        LocalDateTime dataDevolucaoPrevista = dataEmprestimo.plusDays(7);

        emprestimo.setDataEmprestimo(dataEmprestimo);
        emprestimo.setDataDevolucaoPrevista(dataDevolucaoPrevista);
        emprestimo.setStatusDevolucao(false);

        Emprestimo salvo = emprestimoRepository.save(emprestimo);
        return converterParaDTO(salvo);
    }

    @Transactional
    public EmprestimoResponseDTO renovar(Integer id) {
        Emprestimo emprestimo = emprestimoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empréstimo não encontrado."));

        if (emprestimo.getDataDevolucaoPrevista().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Renovação negada: o empréstimo está com prazo vencido.");
        }

        LocalDateTime novaDataDevolucao = emprestimo.getDataDevolucaoPrevista().plusDays(7);
        emprestimo.setDataDevolucaoPrevista(novaDataDevolucao);

        Emprestimo atualizado = emprestimoRepository.save(emprestimo);
        return converterParaDTO(atualizado);
    }

private EmprestimoResponseDTO converterParaDTO(Emprestimo emprestimo) {
        Exemplar exemplar = emprestimo.getExemplar();
        Leitor leitor = emprestimo.getLeitor();

        return new EmprestimoResponseDTO(
                emprestimo.getEmprestimoId(),
                exemplar != null ? exemplar.getExemplarId() : null,
                null, // tombo
                exemplar != null && exemplar.getLivro() != null ? exemplar.getLivro().getLivroId() : null,
                exemplar != null && exemplar.getLivro() != null ? exemplar.getLivro().getTitulo() : null,
                leitor != null ? leitor.getId() : null,
                null,
                emprestimo.getDataEmprestimo(),
                emprestimo.getDataDevolucaoPrevista(),
                emprestimo.getDataDevolucao(),
                emprestimo.getStatusDevolucao()
        );
    }
}
