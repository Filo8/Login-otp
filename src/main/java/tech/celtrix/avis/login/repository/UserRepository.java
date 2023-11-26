package tech.celtrix.avis.login.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import tech.celtrix.avis.login.entite.User;

public interface UserRepository extends CrudRepository<User, Integer> {
  Optional<User> findByEmail(String email);
}
