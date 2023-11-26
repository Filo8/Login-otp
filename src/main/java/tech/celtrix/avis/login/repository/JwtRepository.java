package tech.celtrix.avis.login.repository;

import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import tech.celtrix.avis.login.entite.Jwt;

public interface JwtRepository extends CrudRepository<Jwt, Integer> {
  Optional<Jwt> findByValeurAndDesactiveAndExpire(
    String valeur,
    boolean desactive,
    boolean expire
  );

  @Query(
    "FROM Jwt j WHERE j.expire = :expire AND j.desactive = :desactive AND j.User.email = :email"
  )
  Optional<Jwt> findUserValidToken(
    String email,
    boolean desactive,
    boolean expire
  );

  @Query("FROM Jwt j WHERE j.User.email = :email")
  Stream<Jwt> findUser(String email);

  @Query("FROM Jwt j WHERE j.refreshToken.valeur = :valeur")
  Optional<Jwt> findByRefreshToken(String valeur);

  void deleteAllByExpireAndDesactive(boolean expire, boolean desactive);
}
