package sgab.sgab.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import sgab.sgab.services.EmailService;

/**
 * Envia os e-mails do sistema a partir dos eventos de negócio.
 *
 * AFTER_COMMIT: só envia se a transação foi confirmada (se o cadastro falhar, nada é enviado).
 * @Async: o envio roda em outra thread, então a requisição não espera o SMTP
 * e uma falha no e-mail não desfaz nem quebra a operação.
 */
@Component
public class NotificacaoEmailListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoEmailListener.class);

    private final EmailService emailService;

    public NotificacaoEmailListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoCadastrarUsuario(UsuarioCadastradoEvent evento) {
        try {
            emailService.enviarEmailCriacaoConta(evento.email(), evento.nome());
            log.info("E-mail de criação de conta enviado para {}", evento.email());
        } catch (MailException e) {
            log.warn("Falha ao enviar e-mail de criação de conta para {}: {}", evento.email(), e.getMessage());
        }
    }
}
