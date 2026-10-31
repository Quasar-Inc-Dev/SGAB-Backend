package sgab.sgab.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String remetente;
    private final String urlFrontend;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // O remetente precisa ser a mesma conta autenticada no SMTP, senão o servidor recusa o envio
    public EmailService(JavaMailSender mailSender,
            @Value("${sgab.email.remetente:${spring.mail.username}}") String remetente,
            @Value("${sgab.frontend.url:http://localhost:4200}") String urlFrontend) {
        this.mailSender = mailSender;
        this.remetente = remetente;
        this.urlFrontend = urlFrontend;
    }


    private void enviarMensagem(String destinatario, String assunto, String texto) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(remetente);
        message.setTo(destinatario);
        message.setSubject(assunto);
        message.setText(texto);
        mailSender.send(message);
    }

    public void enviarEmailCriacaoConta(String destinatario, String nomeUsuario) {
        String assunto = "Bem-vindo ao SGAB - Conta Criada com Sucesso!";
        String texto = "Olá, " + nomeUsuario + "!\n\n" +
                "Sua conta no Sistema de Gestão Acadêmica de Biblioteca (SGAB) foi criada com sucesso.\n" +
                "Agora você já pode acessar a plataforma para realizar empréstimos e reservas.";
        enviarMensagem(destinatario, assunto, texto);
    }

    public void enviarEmailRedefinicaoSenha(String destinatario, String token) {
        String assunto = "Redefinição de Senha - SGAB";
        String linkFrontend = urlFrontend + "/redefinir-senha?token=" + token;
        String texto = "Olá,\n\nRecebemos uma solicitação para alterar a sua senha no SGAB.\n\n" +
                "Clique no link abaixo para criar uma nova senha:\n" + linkFrontend + "\n\n" +
                "O link vale por tempo limitado e só pode ser usado uma vez.\n" +
                "Se você não solicitou isso, ignore este e-mail.";
        enviarMensagem(destinatario, assunto, texto);
    }

    public void enviarEmailEmprestimoAprovado(String destinatario, String nomeLeitor, String tituloLivro, LocalDateTime dataLimiteRetirada) {
        String assunto = "Empréstimo Aprovado - Retire seu Livro - SGAB";
        String texto = "Olá, " + nomeLeitor + ",\n\n" +
                "Sua solicitação de empréstimo do livro \"" + tituloLivro + "\" foi aprovada!\n" +
                "Um exemplar já está separado para você no balcão da biblioteca.\n\n" +
                "Retire o livro até: " + dataLimiteRetirada.format(formatter) + "\n" +
                "O prazo de devolução começa a contar a partir da retirada.\n\n" +
                "Caso não retire até esta data, a solicitação será cancelada automaticamente.";
        enviarMensagem(destinatario, assunto, texto);
    }

    public void enviarEmailEmprestimoRecusado(String destinatario, String nomeLeitor, String tituloLivro, String motivo) {
        String assunto = "Solicitação de Empréstimo Recusada - SGAB";
        String texto = "Olá, " + nomeLeitor + ",\n\n" +
                "Infelizmente, sua solicitação de empréstimo para o livro \"" + tituloLivro + "\" não pôde ser atendida.\n" +
                "Motivo: " + motivoOuPadrao(motivo);
        enviarMensagem(destinatario, assunto, texto);
    }

    public void enviarEmailEmprestimoRegistrado(String destinatario, String nomeLeitor, String tituloLivro, LocalDateTime dataDevolucao) {
        String assunto = "Empréstimo Registrado - SGAB";
        String texto = "Olá, " + nomeLeitor + ",\n\n" +
                "A retirada do livro \"" + tituloLivro + "\" foi registrada com sucesso!\n" +
                "Data limite para devolução: " + dataDevolucao.format(formatter) + "\n\n" +
                "Bom proveito da leitura!";
        enviarMensagem(destinatario, assunto, texto);
    }

    public void enviarEmailRetiradaExpirada(String destinatario, String nomeLeitor, String tituloLivro) {
        String assunto = "Prazo de Retirada Expirado - SGAB";
        String texto = "Olá, " + nomeLeitor + ",\n\n" +
                "O prazo para retirar o livro \"" + tituloLivro + "\" terminou e o exemplar separado foi liberado.\n" +
                "Se ainda tiver interesse, faça uma nova solicitação pelo sistema.";
        enviarMensagem(destinatario, assunto, texto);
    }

    public void enviarEmailReservaAprovada(String destinatario, String nomeLeitor, String tituloLivro, LocalDateTime dataLimiteRetirada) {
        String assunto = "Reserva Disponível para Retirada - SGAB";
        String texto = "Olá, " + nomeLeitor + ",\n\n" +
                "O exemplar reservado do livro \"" + tituloLivro + "\" já está separado e disponível!\n" +
                "Você tem até o dia " + dataLimiteRetirada.format(formatter) + " para comparecer à biblioteca e retirá-lo.\n\n" +
                "Caso não retire até esta data, a reserva poderá ser cancelada.";
        enviarMensagem(destinatario, assunto, texto);
    }

    public void enviarEmailReservaNaFila(String destinatario, String nomeLeitor, String tituloLivro) {
        String assunto = "Reserva Confirmada - SGAB";
        String texto = "Olá, " + nomeLeitor + ",\n\n" +
                "Sua reserva do livro \"" + tituloLivro + "\" foi registrada.\n" +
                "O exemplar está em uso no momento e você entrou na fila de espera.\n" +
                "Avisaremos por e-mail quando ele estiver separado para você.";
        enviarMensagem(destinatario, assunto, texto);
    }

    public void enviarEmailReservaRecusada(String destinatario, String nomeLeitor, String tituloLivro, String motivo) {
        String assunto = "Solicitação de Reserva Recusada - SGAB";
        String texto = "Olá, " + nomeLeitor + ",\n\n" +
                "Sua solicitação de reserva para o livro \"" + tituloLivro + "\" foi recusada.\n" +
                "Motivo: " + motivoOuPadrao(motivo);
        enviarMensagem(destinatario, assunto, texto);
    }

    public void enviarEmailAlertaPrazoEntrega(String destinatario, String nomeLeitor, String tituloLivro, LocalDateTime dataDevolucao) {
        String assunto = "Lembrete: Prazo de Entrega Próximo do Vencimento - SGAB";
        String texto = "Olá, " + nomeLeitor + ",\n\n" +
                "Este é um lembrete amigável de que o prazo de devolução do livro \"" + tituloLivro + "\" vence em breve.\n" +
                "Data limite: " + dataDevolucao.format(formatter) + "\n\n" +
                "Lembre-se de realizar a renovação pelo sistema caso precise de mais tempo.";
        enviarMensagem(destinatario, assunto, texto);
    }

    private String motivoOuPadrao(String motivo) {
        return motivo == null || motivo.isBlank() ? "não informado" : motivo;
    }
}
