package tech.celtrix.avis.controleur;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tech.celtrix.avis.login.dto.AuthentificationDTO;
import tech.celtrix.avis.login.entite.User;
import tech.celtrix.avis.login.jwt.JwtService;
import tech.celtrix.avis.login.service.UserService;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
public class UtilisationController {
  private AuthenticationManager authenticationManager;
  private UserService UserService;
  private JwtService jwtService;

  /* 
  @PostMapping(path = "signup")
  public ResponseEntity<Map<String, Object>> inscription(
    @RequestBody User User
  ) {
    Map<String, Object> responseMap = new HashMap<>();
    HttpStatus httpStatus;

    try {
      this.UserService.inscription(User);
      httpStatus = HttpStatus.CREATED;
    } catch (RuntimeException e) {
      httpStatus = HttpStatus.BAD_REQUEST;
    }

    responseMap.put("status", httpStatus.value());
    responseMap.put(
      "message",
      httpStatus == HttpStatus.CREATED
        ? "Registrazione avvenuta con successo"
        : "Registrazione fallita a causa di un errore"
    );

    return new ResponseEntity<>(responseMap, httpStatus);
  }
*/
  @PostMapping(path = "signup")
  public ResponseEntity<Map<String, String>> inscription(@RequestBody User user) {
    log.info("Inscrizione");

    return this.UserService.registrazioneUtente(user);
  }

  @PostMapping(path = "signin")
  public ResponseEntity<?> connexion(
    @RequestBody AuthentificationDTO authentificationDTO
  ) {
    try {
      final Authentication authenticate = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
          authentificationDTO.username(),
          authentificationDTO.password()
        )
      );

      if (authenticate.isAuthenticated()) {
        Map<String, Object> response = Map.of(
          "status",
          HttpStatus.OK.value(),
          "message",
          "Login avvenuto con successo",
          "token",
          jwtService.generate(authentificationDTO.username())
        );
        return ResponseEntity.ok(response);
      }
    } catch (LockedException | DisabledException e) {
      HttpStatus status = HttpStatus.UNAUTHORIZED;
      //String message = e.getClass().getSimpleName(); // Usa il nome dell'eccezione come messaggio
      return ResponseEntity
        .status(status)
        .body(Map.of("status", status.value(), "message", "Reset la Password"));
    } catch (Exception e) {
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      return ResponseEntity
        .status(status)
        .body(
          Map.of(
            "status",
            status.value(),
            "message",
            "Errore durante l'autenticazione"
          )
        );
    }

    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(
        Map.of(
          "status",
          HttpStatus.UNAUTHORIZED.value(),
          "message",
          "Autenticazione fallita"
        )
      );
  }

  /*
  // feature/deconnexion
  @PostMapping(path = "signin")
  public ResponseEntity<?> connexion(
@RequestBody AuthentificationDTO authentificationDTO
  ) {
try {
  final Authentication authenticate = authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(
      authentificationDTO.username(),
      authentificationDTO.password()
    )
  );

  if (authenticate.isAuthenticated()) {
    Map<String, String> token = jwtService.generate(
      authentificationDTO.username()
    );
    return ResponseEntity.ok(Map.of("token", token));
  } else {
    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(Map.of("message", "Autenticazione fallita"));
  }
} catch (Exception e) {
  return ResponseEntity
    .status(HttpStatus.INTERNAL_SERVER_ERROR)
    .body(Map.of("message", "Errore durante l'autenticazione"));
}
  }
*/
/*
  //attivare utilisatore
  @PostMapping(path = "otp")
  public ResponseEntity<Map<String, Object>> activation(
    @RequestBody Map<String, String> activation
  ) {
    try {
      this.UserService.activation(activation);

      Map<String, Object> response = Map.of(
        "status",
        HttpStatus.OK.value(),
        "message",
        "Activation successful"
      );
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error activating user", e);
      Map<String, Object> errorResponse = Map.of(
        "status",
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "message",
        "Error activating user"
      );
      return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(errorResponse);
    }
  }
  */


  
  //cambio password
  @PostMapping(path = "m-password")
  public void modifierMotDePasse(@RequestBody Map<String, String> activation) {
    this.UserService.modifierMotDePasse(activation);
  }

  //nuovo password
  @PostMapping(path = "n-password")
  public void nouveauMotDePasse(@RequestBody Map<String, String> activation) {
    this.UserService.nouveauMotDePasse(activation);
  }

  @PostMapping(path = "refresh-token")
  public @ResponseBody Map<String, String> refreshToken(
    @RequestBody Map<String, String> refreshTokenRequest
  ) {
    return this.jwtService.refreshToken(refreshTokenRequest);
  }

  @PostMapping(path = "logout")
  public void deconnexion() {
    this.jwtService.deconnexion();
  }
}
