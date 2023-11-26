package tech.celtrix.avis.login.service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ResponseStatusException;
import tech.celtrix.avis.TypeDeRole;
import tech.celtrix.avis.login.entite.Role;
import tech.celtrix.avis.login.entite.User;
import tech.celtrix.avis.login.entite.Validation;
import tech.celtrix.avis.login.repository.UserRepository;
import tech.celtrix.avis.util.TemporaryPasswordGenerator;

@AllArgsConstructor
@Service
public class UserService implements UserDetailsService {
  private UserRepository userRepository;
  private BCryptPasswordEncoder passwordEncoder;
  private ValidationService validationService;
  private TemporaryPasswordGenerator temporaryPasswordGenerator;

  // ...

  public void inscription(User User) {
    final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";

    String email = User.getEmail();

    if (!email.matches(EMAIL_PATTERN)) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Indirizzo email non valido"
      );
    }

    if (userRepository.findByEmail(email).isPresent()) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "Indirizzo email già registrato"
      );
    }

    //CryptPassword
    String mdpCrypte = passwordEncoder.encode(User.getMdp());
    User.setMdp(mdpCrypte);

    Role roleUser = new Role();
    roleUser.setLibelle(TypeDeRole.User);
    User.setRole(roleUser);

    User = userRepository.save(User);
    validationService.enregistrer(User);
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
    this.validationService.enregistrer(User);
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
