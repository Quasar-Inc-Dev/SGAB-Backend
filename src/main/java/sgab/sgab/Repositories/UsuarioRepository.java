package sgab.sgab.Repositories;

import sgab.sgab.entities.Usuario;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    @Query("SELECT u FROM Usuario u WHERE u.cpf = :cpf")
    List<Usuario> buscarUsuarioPorCPF(@Param("CPF") String CPF);

    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
}
