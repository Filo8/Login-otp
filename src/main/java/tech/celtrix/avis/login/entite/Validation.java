package tech.celtrix.avis.login.entite;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "validation")
public class Validation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private Instant creation;
  private Instant expiration;
  private Instant activation;
  private String code;

  @OneToOne(cascade = { CascadeType.MERGE, CascadeType.DETACH })
  private User User;
}
