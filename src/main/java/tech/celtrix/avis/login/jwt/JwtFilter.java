package tech.celtrix.avis.login.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.celtrix.avis.login.entite.Jwt;
import tech.celtrix.avis.login.entite.User;
import tech.celtrix.avis.login.service.UserService;

@Service
public class JwtFilter extends OncePerRequestFilter {
  private UserService UserService;
  private JwtService jwtService;

  public JwtFilter(UserService UserService, JwtService jwtService) {
    this.UserService = UserService;
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  )
    throws ServletException, IOException {
    String token;
    Jwt tokenDansLaBDD = null;
    String username = null;
    boolean isTokenExpired = true;

    // Bearer eyJhbGciOiJIUzI1NiJ9.eyJub20iOiJBY2hpbGxlIE1CT1VHVUVORyIsImVtYWlsIjoiYWNoaWxsZS5tYm91Z3VlbmdAY2hpbGxvLnRlY2gifQ.zDuRKmkonHdUez-CLWKIk5Jdq9vFSUgxtgdU1H2216U
    final String authorization = request.getHeader("Authorization");
    if (authorization != null && authorization.startsWith("Bearer ")) {
      token = authorization.substring(7);
      tokenDansLaBDD = this.jwtService.tokenByValue(token);
      isTokenExpired = jwtService.isTokenExpired(token);
      username = jwtService.extractUsername(token);
    }

    if (
      !isTokenExpired &&
      tokenDansLaBDD.getUser().getEmail().equals(username) &&
      SecurityContextHolder.getContext().getAuthentication() == null
    ) {
      UserDetails userDetails = UserService.loadUserByUsername(username);
      UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails.getAuthorities()
      );
      SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    filterChain.doFilter(request, response);
  }
}
