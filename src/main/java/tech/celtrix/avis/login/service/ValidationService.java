package tech.celtrix.avis.login.service;

import static java.time.temporal.ChronoUnit.MINUTES;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ResponseStatusException;
import tech.celtrix.avis.login.entite.User;
import tech.celtrix.avis.login.entite.Validation;
import tech.celtrix.avis.login.repository.ValidationRepository;

@Transactional
@Slf4j
@AllArgsConstructor
@Service
public class ValidationService {
  private ValidationRepository validationRepository;
  private NotificationService notificationService;

  public ResponseEntity<Map<String, String>> enregistrer(User User) {
    Validation validation = new Validation();
    validation.setUser(User);
    Instant creation = Instant.now();
    validation.setCreation(creation);

    Instant expiration = creation.plus(10, ChronoUnit.MINUTES);
    validation.setExpiration(expiration);

    String code = generareCodiceValidazione();
    validation.setCode(code);

    log.debug("Codice di validazione generato: " + code);


    try {
      validationRepository.save(validation);
      // Invia la notifica
      notificationService.envoyer(validation);

      // Creare una mappa contenente il codice e restituirla come JSON
      Map<String, String> response = Collections.singletonMap("code", code);
      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private String generareCodiceValidazione() {
    Random random = new Random();
    int randomInteger = random.nextInt(1_000_000); // Genera un numero tra 0 e 999999
    return String.format("%06d", randomInteger);
  }

  public Validation lireEnFonctionDuCode(String code) {
    return this.validationRepository.findByCode(code)
      .orElseThrow(() -> new RuntimeException("code invalid"));
  }

  @Scheduled(cron = "*/30 * * * * *")
  public void nettoyerTable() {
    final Instant now = Instant.now();
    log.info("Cancelazione dei token à {}", now);
    this.validationRepository.deleteAllByExpirationBefore(now);
  }
}
