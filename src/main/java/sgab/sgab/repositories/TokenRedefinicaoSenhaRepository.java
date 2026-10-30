package sgab.sgab.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sgab.sgab.entities.TokenRedefinicaoSenha;

public interface TokenRedefinicaoSenhaRepository extends JpaRepository<TokenRedefinicaoSenha, Integer> {

    Optional<TokenRedefinicaoSenha> findByTokenHash(String tokenHash);

    // Ao pedir um link novo, os anteriores ainda não usados deixam de valer
    void deleteByUsuarioIdAndDataUsoIsNull(Integer usuarioId);
}
