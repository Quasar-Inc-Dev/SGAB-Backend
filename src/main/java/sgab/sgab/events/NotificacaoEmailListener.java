package sgab.sgab.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import sgab.sgab.entities.Enum.TipoSolicitacao;
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
        enviar("criação de conta", evento.email(),
                () -> emailService.enviarEmailCriacaoConta(evento.email(), evento.nome()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoSolicitarRedefinicaoSenha(RedefinicaoSenhaSolicitadaEvent evento) {
        enviar("redefinição de senha", evento.email(),
                () -> emailService.enviarEmailRedefinicaoSenha(evento.email(), evento.token()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoAprovarEmprestimo(EmprestimoAprovadoEvent evento) {
        enviar("empréstimo aprovado", evento.email(),
                () -> emailService.enviarEmailEmprestimoAprovado(
                        evento.email(), evento.nome(), evento.tituloLivro(), evento.dataLimiteRetirada()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoRecusarSolicitacao(SolicitacaoRecusadaEvent evento) {
        if (evento.tipo() == TipoSolicitacao.EMPRESTIMO) {
            enviar("empréstimo recusado", evento.email(),
                    () -> emailService.enviarEmailEmprestimoRecusado(
                            evento.email(), evento.nome(), evento.tituloLivro(), evento.motivo()));
        } else {
            enviar("reserva recusada", evento.email(),
                    () -> emailService.enviarEmailReservaRecusada(
                            evento.email(), evento.nome(), evento.tituloLivro(), evento.motivo()));
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoRegistrarEmprestimo(EmprestimoRegistradoEvent evento) {
        enviar("empréstimo registrado", evento.email(),
                () -> emailService.enviarEmailEmprestimoRegistrado(
                        evento.email(), evento.nome(), evento.tituloLivro(), evento.dataDevolucaoPrevista()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoExpirarRetirada(RetiradaExpiradaEvent evento) {
        enviar("retirada expirada", evento.email(),
                () -> emailService.enviarEmailRetiradaExpirada(evento.email(), evento.nome(), evento.tituloLivro()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoSepararReserva(ReservaDisponivelEvent evento) {
        enviar("reserva disponível", evento.email(),
                () -> emailService.enviarEmailReservaAprovada(
                        evento.email(), evento.nome(), evento.tituloLivro(), evento.dataLimiteRetirada()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoEntrarNaFila(ReservaNaFilaEvent evento) {
        enviar("reserva na fila", evento.email(),
                () -> emailService.enviarEmailReservaNaFila(evento.email(), evento.nome(), evento.tituloLivro()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoAproximarPrazoDevolucao(PrazoDevolucaoProximoEvent evento) {
        enviar("lembrete de devolução", evento.email(),
                () -> emailService.enviarEmailAlertaPrazoEntrega(
                        evento.email(), evento.nome(), evento.tituloLivro(), evento.dataDevolucaoPrevista()));
    }

    private void enviar(String descricao, String email, Runnable envio) {
        try {
            envio.run();
            log.info("E-mail de {} enviado para {}", descricao, email);
        } catch (MailException e) {
            log.warn("Falha ao enviar e-mail de {} para {}: {}", descricao, email, e.getMessage());
        }
    }
}
