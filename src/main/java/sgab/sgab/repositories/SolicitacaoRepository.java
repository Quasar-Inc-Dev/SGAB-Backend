package sgab.sgab.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import sgab.sgab.entities.Solicitacao;
import sgab.sgab.entities.Enum.StatusSolicitacao;
import sgab.sgab.entities.Enum.TipoSolicitacao;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Integer> {

    // Os @EntityGraph trazem tudo o que o SolicitacaoResponseDTO usa. Empréstimo e reserva não entram:
    // o DTO só usa o id deles, e o id de uma associação LAZY é lido sem consultar o banco.

    // Mais antigas primeiro: o funcionário atende por ordem de chegada
    @EntityGraph(attributePaths = {"leitor", "leitor.usuario", "livro", "exemplar", "analisadoPor", "retiradaConfirmadaPor"})
    Page<Solicitacao> findByStatusOrderByDataSolicitacaoAsc(StatusSolicitacao status, Pageable pageable);

    @EntityGraph(attributePaths = {"leitor", "leitor.usuario", "livro", "exemplar", "analisadoPor", "retiradaConfirmadaPor"})
    Page<Solicitacao> findByStatusAndTipoOrderByDataSolicitacaoAsc(StatusSolicitacao status, TipoSolicitacao tipo, Pageable pageable);

    @EntityGraph(attributePaths = {"leitor", "leitor.usuario", "livro", "exemplar", "analisadoPor", "retiradaConfirmadaPor"})
    Page<Solicitacao> findByLeitorIdOrderByDataSolicitacaoDesc(Integer leitorId, Pageable pageable);

    boolean existsByLeitorIdAndLivroLivroIdAndTipoAndStatus(
            Integer leitorId, Integer livroId, TipoSolicitacao tipo, StatusSolicitacao status
    );

    boolean existsByLeitorIdAndExemplarExemplarIdAndTipoAndStatus(
            Integer leitorId, Integer exemplarId, TipoSolicitacao tipo, StatusSolicitacao status
    );

    boolean existsByLivroLivroId(Integer livroId);

    // Solicitações cujo prazo de retirada já passou (o agendador usa leitor, livro e exemplar)
    @EntityGraph(attributePaths = {"leitor", "leitor.usuario", "livro", "exemplar"})
    List<Solicitacao> findByStatusAndDataLimiteRetiradaBefore(StatusSolicitacao status, LocalDateTime limite);
}
