package tech.celtrix.avis.login.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.celtrix.avis.login.entite.Avis;

public interface AvisRepository extends JpaRepository<Avis, Integer> {}
