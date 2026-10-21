package sgab.sgab.repositories;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sgab.sgab.entities.Reserva;
import sgab.sgab.entities.Enum.StatusReserva;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    boolean existsByExemplarLivroLivroId(Integer livroId);

    boolean existsByExemplarExemplarIdAndStatus(Integer exemplarId, StatusReserva status);

    boolean existsByExemplarExemplarIdAndLeitorIdAndStatusIn(Integer exemplarId, Integer leitorId, Collection<StatusReserva> status);

    // Primeiro da fila do exemplar: a reserva ativa mais antiga
    Optional<Reserva> findFirstByExemplarExemplarIdAndStatusOrderByDataReservaAsc(Integer exemplarId, StatusReserva status);

    // Exemplar do livro que está separado para o leitor
    Optional<Reserva> findFirstByExemplarLivroLivroIdAndLeitorIdAndStatus(Integer livroId, Integer leitorId, StatusReserva status);
}
