package tech.celtrix.avis.login.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import tech.celtrix.avis.TypeDeRole;
import tech.celtrix.avis.login.entite.Role;
import tech.celtrix.avis.login.entite.User;
import tech.celtrix.avis.login.entite.Validation;
import tech.celtrix.avis.login.repository.UserRepository;


@AllArgsConstructor
@Service
public class UserService implements UserDetailsService {
  private UserRepository userRepository;
  private BCryptPasswordEncoder passwordEncoder;
  private ValidationService validationService;

  public ResponseEntity<Map<String, String>> registrazioneUtente(User utente) {
    final String PATTERN_EMAIL = "^[A-Za-z0-9+_.-]+@(.+)$";

    Map<String, String> response = new HashMap<>();

    // Validazione dell'indirizzo email
    String email = utente.getEmail();
    if (!email.matches(PATTERN_EMAIL)) {
      response.put("status", "400");
      response.put("error", "Indirizzo email non valido");
      return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Verifica se l'indirizzo email è già registrato
    if (userRepository.findByEmail(email).isPresent()) {
      response.put("status", "403");
      response.put("error", "Indirizzo email già registrato");
      return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    try {
      // Crittografia della password
      String passwordCifrata = passwordEncoder.encode(utente.getMdp());
      utente.setMdp(passwordCifrata);

      // Assegnamento del ruolo utente
      Role ruoloUtente = new Role();
      ruoloUtente.setLibelle(TypeDeRole.User);
      utente.setRole(ruoloUtente);

      // Salvataggio dell'utente nel repository
      userRepository.save(utente);

      // Esecuzione del servizio di validazione
      validationService.registra(utente);

      // Restituisci un messaggio di successo
      response.put("status", "201");
      response.put("message", "Registrazione avvenuta con successo!");
      return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    } catch (Exception e) {
      // In caso di errore, restituisci un'eccezione con un messaggio appropriato
      response.put("status", "500");
      response.put(
        "error",
        "Errore durante la registrazione: " + e.getMessage()
      );
      return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public void activation(Map<String, String> activation) {
    Validation validation =
      this.validationService.lireEnFonctionDuCode(activation.get("code"));
    if (Instant.now().isAfter(validation.getExpiration())) {
      throw new RuntimeException("Sessione scaduta!");
    }
    User UserActiver =
      this.userRepository.findById(validation.getUser().getId())
        .orElseThrow(() -> new RuntimeException("User inconnu"));
    UserActiver.setActif(true);
    this.userRepository.save(UserActiver);
  }

  @Override
  public User loadUserByUsername(String username)
    throws UsernameNotFoundException {
    return this.userRepository.findByEmail(username)
      .orElseThrow(
        () -> new UsernameNotFoundException("Utilisatore non essiste ")
      );
  }

  //Cambio pwd
  public ResponseEntity<Map<String, String>> modifierMotDePasse(
    Map<String, String> parametres
  ) {
    String email = parametres.get("email");

    // Carica l'utente dal database utilizzando l'email fornita
    User user = loadUserByUsername(email);

    Map<String, String> response = new HashMap<>();

    try {
      if (user != null) {
        // Registra la validazione dell'utente
        validationService.registra(user);

        // Messaggio di successo
        String successMessage =
          "Validazione registrata con successo per l'utente con email: " +
          email;

        response.put("status", HttpStatus.OK.toString());
        response.put("message", successMessage);

        // Restituisci una risposta di successo
        return new ResponseEntity<>(response, HttpStatus.OK);
      } else {
        // Messaggio di errore
        String errorMessage = "Utente non trovato per l'email: " + email;

        response.put("status", HttpStatus.NOT_FOUND.toString());
        response.put("error", errorMessage);

        // Restituisci una risposta di errore se l'utente non è stato trovato
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      // In caso di errore, restituisci un'eccezione con un messaggio appropriato
      response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.toString());
      response.put(
        "error",
        "Errore durante la modifica della password: " + e.getMessage()
      );
      return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public ResponseEntity<String> newPassword(Map<String, String> parametres) {
    // Carica l'utente dal repository utilizzando l'email fornita
    User user = this.loadUserByUsername(parametres.get("email"));

    // Ottieni l'oggetto Validation utilizzando il codice fornito
    final Validation validation = validationService.lireEnFonctionDuCode(
      parametres.get("code")
    );

    // Creazione della ResponseEntity di risposta
    ResponseEntity<String> responseEntity;

    // Verifica che l'utente associato alla validazione abbia la stessa email dell'utente corrente
    if (validation.getUser().getEmail().equals(user.getEmail())) {
      // Critta la nuova password fornita
      String mdpCrypte =
        this.passwordEncoder.encode(parametres.get("password"));

      // Imposta la nuova password crittografata per l'utente
      user.setMdp(mdpCrypte);

      // Salva le modifiche dell'utente nel repository
      this.userRepository.save(user);

      // Messaggio di successo
      String successMessage =
        "Password modificata con successo - Stato: " + HttpStatus.OK;

      // Costruzione della ResponseEntity di successo
      responseEntity =
        ResponseEntity.status(HttpStatus.OK).body(successMessage);
    } else {
      // Messaggio di errore
      String errorMessage =
        "Errore nella modifica della password - Stato: " +
        HttpStatus.BAD_REQUEST +
        " Motivo: L'email dell'utente associato alla validazione non corrisponde all'utente corrente.";

      // Costruzione della ResponseEntity di errore
      responseEntity = ResponseEntity.badRequest().body(errorMessage);
    }

    // Restituisci la ResponseEntity creata
    return responseEntity;
  }
}
