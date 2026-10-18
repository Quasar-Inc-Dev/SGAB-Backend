package sgab.sgab.services;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgab.sgab.repositories.ReservaRepository;
import sgab.sgab.repositories.LivroRepository;
import sgab.sgab.repositories.LeitorRepository;
import sgab.sgab.entities.Reserva;
import sgab.sgab.entities.Livro;
import sgab.sgab.entities.Leitor;
import sgab.sgab.exceptions.LivroNaoEncontradoException;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final LivroRepository livroRepository;
    private final LeitorRepository leitorRepository;

    public ReservaService(
            ReservaRepository reservaRepository,
            LivroRepository livroRepository,
            LeitorRepository leitorRepository
    ) {
        this.reservaRepository = reservaRepository;
        this.livroRepository = livroRepository;
        this.leitorRepository = leitorRepository;
    }

    @Transactional
    public Reserva solicitarReserva(Integer livroId, Integer leitorId) {
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new LivroNaoEncontradoException("Livro não encontrado para realizar a reserva."));

        Leitor leitor = leitorRepository.findById(leitorId)
                .orElseThrow(() -> new RuntimeException("Leitor não encontrado."));

        Reserva reserva = new Reserva();
        reserva.setLivro(livro);
        reserva.setLeitor(leitor);
        reserva.setData_emprestimo(LocalDate.now());

        return reservaRepository.save(reserva);
    }

    public List<Reserva> listarTodas() {
        return reservaRepository.findAll();
    }

    public Reserva buscarPorId(Integer id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada."));
    }

    @Transactional
    public void cancelarReserva(Integer id) {
        Reserva reserva = buscarPorId(id);
        reservaRepository.delete(reserva);
    }
}
