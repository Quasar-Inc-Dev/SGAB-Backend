package sgab.sgab.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sgab.sgab.entities.Exemplar;
import sgab.sgab.entities.Enum.StatusExemplar;

public interface ExemplarRepository extends JpaRepository<Exemplar, Integer> {

    /** Total de exemplares e quantos estão disponíveis, por livro (projeção da consulta abaixo). */
    interface ContagemExemplares {
        Integer getLivroId();
        Long getTotal();
        Long getDisponiveis();
    }

    List<Exemplar> findByLivroLivroId(Integer livroId);

    long countByLivroLivroId(Integer livroId);

    boolean existsByTombo(String tombo);

    long countByLivroLivroIdAndStatus(Integer livroId, StatusExemplar status);

    boolean existsByLivroLivroIdAndStatusNot(Integer livroId, StatusExemplar status);

    Optional<Exemplar> findFirstByLivroLivroIdAndStatusOrderByExemplarIdAsc(Integer livroId, StatusExemplar status);

    // Conta os exemplares de vários livros numa única consulta (evita 2 consultas por livro na listagem).
    // Livros sem exemplares não aparecem no resultado.
    @Query("""
            select e.livro.livroId as livroId,
                   count(e) as total,
                   sum(case when e.status = :disponivel then 1 else 0 end) as disponiveis
            from Exemplar e
            where e.livro.livroId in :livroIds
            group by e.livro.livroId
            """)
    List<ContagemExemplares> contarPorLivros(@Param("livroIds") Collection<Integer> livroIds,
                                             @Param("disponivel") StatusExemplar disponivel);
}
