package com.pfe.convocation.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Envoie une convocation (PDF en pièce jointe) par e-mail via SMTP (Gmail).
 *
 * <p>L'envoi n'est possible que si les identifiants Gmail sont configurés
 * ({@code spring.mail.username} / {@code spring.mail.password}). Sinon {@link #estConfigure()}
 * renvoie {@code false} et le service amont court-circuite l'opération avec un 503 explicite.
 */
@Component
public class ConvocationMailSender {

    private final JavaMailSender mailSender;
    private final String from;
    private final boolean configure;

    public ConvocationMailSender(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String username,
            @Value("${convocation.mail.from:}") String from) {
        this.mailSender = mailSender;
        this.from = (from == null || from.isBlank()) ? username : from;
        this.configure = username != null && !username.isBlank();
    }

    /** Les identifiants SMTP sont-ils renseignés (envoi possible) ? */
    public boolean estConfigure() {
        return configure;
    }

    /**
     * Envoie un e-mail avec le PDF de convocation en pièce jointe.
     *
     * @throws MessagingException si la composition ou l'envoi échoue (e-mail invalide, SMTP KO…)
     */
    public void envoyer(String destinataire, String sujet, String corps, byte[] pdf, String nomFichier)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        if (from != null && !from.isBlank()) {
            helper.setFrom(from);
        }
        helper.setTo(destinataire);
        helper.setSubject(sujet);
        helper.setText(corps, false);
        helper.addAttachment(nomFichier, new ByteArrayResource(pdf));
        mailSender.send(message);
    }
}
