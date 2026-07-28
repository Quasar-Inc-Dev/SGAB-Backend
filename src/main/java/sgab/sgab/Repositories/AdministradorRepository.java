package sgab.sgab.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sgab.sgab.entities.Administrador;

public interface AdministradorRepository extends JpaRepository <Administrador, String> {
    
    @Query("SELECT a from Administrador a where a.cpf = :cpf")
    List<Administrador> buscarAdminPorCPF(@Param("cpf") String cpf);
}
