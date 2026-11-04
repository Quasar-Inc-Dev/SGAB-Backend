package sgab.sgab.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sgab.sgab.entities.Emprestimo;

@Repository
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Integer> {

    // Traz junto (num único SELECT com JOIN) tudo o que o EmprestimoResponseDTO usa
    @Override
    @EntityGraph(attributePaths = {"exemplar", "exemplar.livro", "leitor", "leitor.usuario"})
    Page<Emprestimo> findAll(Pageable pageable);

    boolean existsByExemplarLivroLivroId(Integer livroId);

    // Empréstimos em aberto que vencem dentro do intervalo e ainda não receberam lembrete
    // (o "is null" cobre registros criados antes de a coluna existir)
    @Query("""
            select e from Emprestimo e
            join fetch e.exemplar ex
            join fetch ex.livro
            join fetch e.leitor l
            join fetch l.usuario
            where e.statusDevolucao = false
              and (e.lembretePrazoEnviado is null or e.lembretePrazoEnviado = false)
              and e.dataDevolucaoPrevista between :inicio and :fim
            """)
    List<Emprestimo> buscarSemLembreteComVencimentoEntre(
            @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
