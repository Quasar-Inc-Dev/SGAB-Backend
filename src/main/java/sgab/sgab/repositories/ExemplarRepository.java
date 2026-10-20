package sgab.sgab.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sgab.sgab.entities.Exemplar;
import sgab.sgab.entities.Enum.StatusExemplar;

public interface ExemplarRepository extends JpaRepository<Exemplar, Integer> {

    List<Exemplar> findByLivroLivroId(Integer livroId);

    long countByLivroLivroId(Integer livroId);

    boolean existsByTombo(String tombo);

    long countByLivroLivroIdAndStatus(Integer livroId, StatusExemplar status);

    boolean existsByLivroLivroIdAndStatusNot(Integer livroId, StatusExemplar status);

    Optional<Exemplar> findFirstByLivroLivroIdAndStatusOrderByExemplarIdAsc(Integer livroId, StatusExemplar status);
}
