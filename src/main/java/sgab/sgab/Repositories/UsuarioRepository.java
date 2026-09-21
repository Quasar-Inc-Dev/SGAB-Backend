package sgab.sgab.Repositories;

import sgab.sgab.entities.Usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;



public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByCpf(String cpf);
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
}
