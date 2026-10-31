package sgab.sgab.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import sgab.sgab.services.EmprestimoService;

/**
 * Verifica periodicamente os empréstimos perto do vencimento e avisa o leitor por e-mail.
 */
@Component
public class LembretePrazoDevolucaoScheduler {

    private static final Logger log = LoggerFactory.getLogger(LembretePrazoDevolucaoScheduler.class);

    private final EmprestimoService emprestimoService;

    public LembretePrazoDevolucaoScheduler(EmprestimoService emprestimoService) {
        this.emprestimoService = emprestimoService;
    }

    // Padrão: a cada 1 hora
    @Scheduled(fixedDelayString = "${sgab.emprestimo.intervalo-lembrete-ms:3600000}")
    public void enviarLembretesPrazoDevolucao() {
        int enviados = emprestimoService.enviarLembretesPrazoDevolucao();

        if (enviados > 0) {
            log.info("{} lembrete(s) de prazo de devolução enviado(s)", enviados);
        }
    }
}
