package tech.celtrix.avis.login.entite;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "jwt")
public class Jwt {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private String valeur;
  private boolean desactive;
  private boolean expire;

  @OneToOne(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
  private RefreshToken refreshToken;

  @ManyToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE })
  @JoinColumn(name = "User_id")
  private User User;
}
