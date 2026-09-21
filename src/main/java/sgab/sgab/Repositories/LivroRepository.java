package sgab.sgab.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sgab.sgab.entities.Livro;

public interface LivroRepository extends JpaRepository<Livro, Integer> {

    Optional<Livro> findByISBN(String isbn);

    boolean existsByISBN(String isbn);
}
