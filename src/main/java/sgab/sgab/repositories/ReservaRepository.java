package sgab.sgab.repositories;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sgab.sgab.entities.Reserva;
import sgab.sgab.entities.Enum.StatusReserva;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    boolean existsByLivroLivroId(Integer livroId);

    boolean existsByLivroLivroIdAndStatus(Integer livroId, StatusReserva status);

    boolean existsByLivroLivroIdAndLeitorIdAndStatusIn(Integer livroId, Integer leitorId, Collection<StatusReserva> status);

    long countByLivroLivroIdAndStatus(Integer livroId, StatusReserva status);

    // Primeiro da fila: a reserva ativa mais antiga
    Optional<Reserva> findFirstByLivroLivroIdAndStatusOrderByDataReservaAsc(Integer livroId, StatusReserva status);

    Optional<Reserva> findFirstByLivroLivroIdAndLeitorIdAndStatus(Integer livroId, Integer leitorId, StatusReserva status);
}
