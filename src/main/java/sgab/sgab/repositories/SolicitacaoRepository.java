package sgab.sgab.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import sgab.sgab.entities.Solicitacao;
import sgab.sgab.entities.Enum.StatusSolicitacao;
import sgab.sgab.entities.Enum.TipoSolicitacao;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Integer> {

    // Mais antigas primeiro: o funcionário atende por ordem de chegada
    List<Solicitacao> findByStatusOrderByDataSolicitacaoAsc(StatusSolicitacao status);

    List<Solicitacao> findByStatusAndTipoOrderByDataSolicitacaoAsc(StatusSolicitacao status, TipoSolicitacao tipo);

    List<Solicitacao> findByLeitorIdOrderByDataSolicitacaoDesc(Integer leitorId);

    boolean existsByLeitorIdAndLivroLivroIdAndTipoAndStatus(
            Integer leitorId, Integer livroId, TipoSolicitacao tipo, StatusSolicitacao status
    );

    boolean existsByLeitorIdAndExemplarExemplarIdAndTipoAndStatus(
            Integer leitorId, Integer exemplarId, TipoSolicitacao tipo, StatusSolicitacao status
    );

    boolean existsByLivroLivroId(Integer livroId);

    // Solicitações cujo prazo de retirada já passou
    List<Solicitacao> findByStatusAndDataLimiteRetiradaBefore(StatusSolicitacao status, LocalDateTime limite);
}
