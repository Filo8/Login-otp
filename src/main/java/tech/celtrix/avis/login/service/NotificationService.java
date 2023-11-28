package tech.celtrix.avis.login.service;

import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import tech.celtrix.avis.login.entite.Validation;

@AllArgsConstructor
@Service
public class NotificationService {
  JavaMailSender javaMailSender;

  public void envoyer(Validation validation) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("celtrixd@gmail.com");
    message.setTo(validation.getUser().getEmail());
    message.setSubject("Il tuo codice d'attivazione");

    String texte = String.format(
      "Salve %s, <br /> Ecco il tuo codice d'attivazione %s;<br> A presto",
      validation.getUser().getNom(),
      validation.getCode()
    );
    message.setText(texte);

    javaMailSender.send(message);
  }
}
