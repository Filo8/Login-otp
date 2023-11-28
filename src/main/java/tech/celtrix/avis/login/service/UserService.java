package tech.celtrix.avis.login.service;

import java.time.Instant;
import java.util.Map;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import lombok.AllArgsConstructor;
import tech.celtrix.avis.TypeDeRole;
import tech.celtrix.avis.login.entite.Role;
import tech.celtrix.avis.login.entite.User;
import tech.celtrix.avis.login.entite.Validation;
import tech.celtrix.avis.login.repository.UserRepository;
import tech.celtrix.avis.util.TemporaryPasswordGenerator;
import java.util.HashMap;
@AllArgsConstructor
@Service
public class UserService implements UserDetailsService {
  private UserRepository userRepository;
  private BCryptPasswordEncoder passwordEncoder;
  private ValidationService validationService;
  private TemporaryPasswordGenerator temporaryPasswordGenerator;

  
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
        response.put("error", "Errore durante la registrazione: " + e.getMessage());
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

  public void modifierMotDePasse(Map<String, String> parametres) {
    User User = this.loadUserByUsername(parametres.get("email"));
    this.validationService.registra(User);
  }

  public void nouveauMotDePasse(Map<String, String> parametres) {
    User User = this.loadUserByUsername(parametres.get("email"));
    final Validation validation = validationService.lireEnFonctionDuCode(
      parametres.get("code")
    );
    if (validation.getUser().getEmail().equals(User.getEmail())) {
      String mdpCrypte =
        this.passwordEncoder.encode(parametres.get("password"));
      User.setMdp(mdpCrypte);
      this.userRepository.save(User);
    }
  }
}
