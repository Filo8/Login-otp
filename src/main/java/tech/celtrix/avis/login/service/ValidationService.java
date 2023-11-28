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
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

  /**
   * Registra un utente e genera un codice di validazione.
   *
   * @param user L'utente da registrare.
   * @return ResponseEntity contenente il codice di validazione.
   */
  public ResponseEntity<Map<String, String>> registra(User user) {
    try {
      Validation validation = new Validation();
      validation.setUser(user);
      Instant creation = Instant.now();
      validation.setCreation(creation);

      Instant expiration = creation.plus(10, MINUTES);
      validation.setExpiration(expiration);

      String code = generareCodiceValidazione();
      validation.setCode(code);

      log.debug("Codice di validazione generato: " + code);

      validationRepository.save(validation);
      // Invia la notifica
      notificationService.envoyer(validation);

      // Creare una mappa contenente il codice e restituirla come JSON
      Map<String, String> response = Collections.singletonMap("code", code);
      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
      log.error("Errore durante la registrazione dell'utente", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore durante la registrazione dell'utente", e);
    }
  }

  /**
   * Genera un codice di validazione a sei cifre.
   *
   * @return Il codice di validazione generato.
   */
  private String generareCodiceValidazione() {
    try {
      Random random = new Random();
      int randomInteger = random.nextInt(1_000_000); // Genera un numero tra 0 e 999999
      return String.format("%06d", randomInteger);
    } catch (Exception e) {
      log.error("Errore durante la generazione del codice di validazione", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore durante la generazione del codice di validazione", e);
    }
  }

  /**
   * Legge una validazione in base al codice.
   *
   * @param code Il codice di validazione.
   * @return La validazione corrispondente al codice.
   * @throws ResponseStatusException Se il codice non è valido.
   */
  public Validation lireEnFonctionDuCode(String code) {
    return this.validationRepository.findByCode(code)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Codice di validazione non valido"));
  }

  /**
   * Schedulato per eseguire la pulizia della tabella ogni 3 secondi.
   */
  @Scheduled(cron = "*/40 * * * * *")
  public void nettoyerTable() {
    try {
      final Instant now = Instant.now();
      log.info("Cancellazione dei token a {}", now);
      this.validationRepository.deleteAllByExpirationBefore(now);
    } catch (Exception e) {
      log.error("Errore durante la pulizia della tabella delle validazioni", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore durante la pulizia della tabella delle validazioni", e);
    }
  }
}
