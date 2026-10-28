package sgab.sgab.dtos.response;

import java.time.LocalDateTime;

import sgab.sgab.entities.Enum.StatusSolicitacao;
import sgab.sgab.entities.Enum.TipoSolicitacao;

public record SolicitacaoResponseDTO(
        Integer solicitacaoId,
        TipoSolicitacao tipo,
        StatusSolicitacao status,
        Integer leitorId,
        String nomeLeitor,
        String tipoLeitor,
        Integer livroId,
        String titulo,
        Integer exemplarId,
        String tombo,
        LocalDateTime dataSolicitacao,
        LocalDateTime dataAnalise,
        String nomeAnalista,
        String motivoRecusa,
        LocalDateTime dataLimiteRetirada,
        LocalDateTime dataRetirada,
        String nomeResponsavelRetirada,
        Integer emprestimoId,
        Integer reservaId
) {
}
