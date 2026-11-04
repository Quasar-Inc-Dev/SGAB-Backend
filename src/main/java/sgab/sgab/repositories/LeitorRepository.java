package sgab.sgab.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import sgab.sgab.entities.Leitor;

public interface LeitorRepository extends JpaRepository<Leitor, Integer> {

    // Nome e e-mail do LeitorResponseDTO ficam no Usuario: vem no mesmo SELECT
    @Override
    @EntityGraph(attributePaths = "usuario")
    Page<Leitor> findAll(Pageable pageable);
}
