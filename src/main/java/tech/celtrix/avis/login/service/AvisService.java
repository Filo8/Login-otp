package tech.celtrix.avis.login.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tech.celtrix.avis.login.entite.Avis;
import tech.celtrix.avis.login.entite.User;
import tech.celtrix.avis.login.repository.AvisRepository;

@AllArgsConstructor
@Service
public class AvisService {
  private final AvisRepository avisRepository;

  public void creer(Avis avis) {
    User User = (User) SecurityContextHolder
      .getContext()
      .getAuthentication()
      .getPrincipal();
    avis.setUser(User);
    this.avisRepository.save(avis);
  }
}
