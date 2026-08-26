package sgab.sgab.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sgab.sgab.entities.Leitor;

public interface LeitorRepository extends JpaRepository<Leitor, Integer> {

    @Query("SELECT l FROM Leitor l Where l.cpf = :cpf")
    List<Leitor> buscarLeitorPorCpf(@Param("cpf") String cpf);
}
