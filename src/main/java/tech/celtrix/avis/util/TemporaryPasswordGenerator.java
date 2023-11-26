package tech.celtrix.avis.util;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class TemporaryPasswordGenerator {
  private static final int PASSWORD_LENGTH = 12;

  public String generateTemporaryPassword() {
    SecureRandom random = new SecureRandom();
    byte[] bytes = new byte[PASSWORD_LENGTH];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
