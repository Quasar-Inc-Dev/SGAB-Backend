package sgab.sgab.services;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgab.sgab.repositories.EmprestimoRepository;
import sgab.sgab.repositories.LivroRepository;
import sgab.sgab.repositories.LeitorRepository;
import sgab.sgab.entities.Emprestimo;
import sgab.sgab.entities.Livro;
import sgab.sgab.entities.Leitor;
import sgab.sgab.exceptions.LivroNaoEncontradoException;

@Service
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final LivroRepository livroRepository;
    private final LeitorRepository leitorRepository;

    public EmprestimoService(
            EmprestimoRepository emprestimoRepository,
            LivroRepository livroRepository,
            LeitorRepository leitorRepository
    ) {
        this.emprestimoRepository = emprestimoRepository;
        this.livroRepository = livroRepository;
        this.leitorRepository = leitorRepository;
    }

    public List<Emprestimo> listarTodos() {
        return emprestimoRepository.findAll();
    }

    public Emprestimo buscarPorId(Integer id) {
        return emprestimoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empréstimo não encontrado."));
    }

    @Transactional
    public Emprestimo registrarEmprestimo(Integer livroId, Integer leitorId) {
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new LivroNaoEncontradoException("Livro não encontrado."));

        Leitor leitor = leitorRepository.findById(leitorId)
                .orElseThrow(() -> new RuntimeException("Leitor não encontrado."));

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setLivro(livro);
        emprestimo.setLeitor(leitor);

        LocalDateTime dataEmprestimo = LocalDateTime.now();
        LocalDateTime dataDevolucaoPrevista = dataEmprestimo.plusDays(7);

        emprestimo.setDataEmprestimo(dataEmprestimo);
        emprestimo.setDataDevolucaoPrevista(dataDevolucaoPrevista);
        emprestimo.setStatusDevolucao(false);

        return emprestimoRepository.save(emprestimo);
    }

    @Transactional
    public Emprestimo renovar(Integer id) {
        Emprestimo emprestimo = buscarPorId(id);

        boolean livroPossuiReservas = verificarSeLivroPossuiReservas(emprestimo.getLivro());

        if (livroPossuiReservas) {
            throw new IllegalArgumentException("Renovação não permitida: o livro possui reservas pendentes na fila de espera.");
        }

        if (emprestimo.getDataDevolucaoPrevista().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Renovação negada: o empréstimo está com prazo vencido.");
        }

        LocalDateTime novaDataDevolucao = emprestimo.getDataDevolucaoPrevista().plusDays(7);
        emprestimo.setDataDevolucaoPrevista(novaDataDevolucao);

        return emprestimoRepository.save(emprestimo);
    }

    private boolean verificarSeLivroPossuiReservas(Livro livro) {
        return false;
    }
}
