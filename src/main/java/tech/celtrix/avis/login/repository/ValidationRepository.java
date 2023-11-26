package tech.celtrix.avis.login.repository;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import tech.celtrix.avis.login.entite.Validation;

public interface ValidationRepository
  extends CrudRepository<Validation, Integer> {
  Optional<Validation> findByCode(String code);

  void deleteAllByExpirationBefore(Instant now);
}
