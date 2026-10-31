package sgab.sgab.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sgab.sgab.entities.Emprestimo;

@Repository
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Integer> {
    // O JpaRepository já nos fornece findAll, findById, save e delete

    boolean existsByExemplarLivroLivroId(Integer livroId);

    // Empréstimos em aberto que vencem dentro do intervalo e ainda não receberam lembrete
    // (o "is null" cobre registros criados antes de a coluna existir)
    @Query("""
            select e from Emprestimo e
            where e.statusDevolucao = false
              and (e.lembretePrazoEnviado is null or e.lembretePrazoEnviado = false)
              and e.dataDevolucaoPrevista between :inicio and :fim
            """)
    List<Emprestimo> buscarSemLembreteComVencimentoEntre(
            @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
