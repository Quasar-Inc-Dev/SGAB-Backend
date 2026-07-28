package sgab.sgab.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sgab.sgab.entities.Funcionario;

public interface FuncionarioRepository extends JpaRepository <Funcionario, String> {
    
    @Query("SELECT f from Funcionario f where f.cpf = :cpf")
    List<Funcionario> buscarFuncionarioPorCPF(@Param("cpf") String cpf);
}
