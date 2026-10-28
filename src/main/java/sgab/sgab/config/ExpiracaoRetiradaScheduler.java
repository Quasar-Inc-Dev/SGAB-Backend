package sgab.sgab.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import sgab.sgab.services.SolicitacaoService;

/**
 * Verifica periodicamente os empréstimos aprovados que não foram retirados no prazo.
 */
@Component
@EnableScheduling
public class ExpiracaoRetiradaScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExpiracaoRetiradaScheduler.class);

    private final SolicitacaoService solicitacaoService;

    public ExpiracaoRetiradaScheduler(SolicitacaoService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }

    // Padrão: a cada 5 minutos
    @Scheduled(fixedDelayString = "${sgab.solicitacao.intervalo-verificacao-ms:300000}")
    public void expirarRetiradasVencidas() {
        int expiradas = solicitacaoService.expirarRetiradasVencidas();

        if (expiradas > 0) {
            log.info("{} solicitação(ões) de empréstimo expirada(s) por falta de retirada", expiradas);
        }
    }
}
