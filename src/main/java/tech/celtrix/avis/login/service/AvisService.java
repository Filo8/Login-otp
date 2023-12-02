package tech.celtrix.avis.login.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tech.celtrix.avis.login.entite.Avis;
import tech.celtrix.avis.login.entite.User;
import tech.celtrix.avis.login.repository.AvisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@AllArgsConstructor
@Service
public class AvisService {
  private final AvisRepository avisRepository;
/*
public void creer(Avis avis) {

  User user = (User) SecurityContextHolder
      .getContext()
      .getAuthentication()
      .getPrincipal();

  avis.setUser(user);

  this.avisRepository.save(avis);
}
*/


// ...

private static final Logger logger = LoggerFactory.getLogger(AvisService.class);

public void creer(Avis avis) {
    logger.debug("Creazione di un nuovo avviso: {}", avis);
    try {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        avis.setUser(user);
        this.avisRepository.save(avis);
    } catch (Exception e) {
        logger.error("Errore durante la creazione dell'avviso", e);
        // Gestisci l'eccezione o lanciala di nuovo se appropriato
    }
}

  
}
