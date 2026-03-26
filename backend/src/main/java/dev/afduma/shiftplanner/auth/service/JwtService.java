package dev.afduma.shiftplanner.auth.service;

import dev.afduma.shiftplanner.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final JwtProperties jwtProperties;
  private final SecretKey signingKey;

  public JwtService(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
    this.signingKey = createSigningKey(jwtProperties.secret());
  }

  public String generateToken(AuthenticatedUser authenticatedUser) {
    Instant now = Instant.now();
    Instant expiresAt = now.plusSeconds(jwtProperties.expirationSeconds());

    return Jwts.builder()
        .subject(authenticatedUser.getUserId().toString())
        .claim("email", authenticatedUser.getUsername())
        .claim(
            "role",
            authenticatedUser.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("ROLE_USER"))
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiresAt))
        .signWith(signingKey)
        .compact();
  }

  public UUID extractUserId(String token) {
    return UUID.fromString(parseClaims(token).getSubject());
  }

  public boolean isTokenValid(String token) {
    try {
      Claims claims = parseClaims(token);
      return claims.getExpiration().after(new Date());
    } catch (JwtException | IllegalArgumentException exception) {
      return false;
    }
  }

  public long getExpirationSeconds() {
    return jwtProperties.expirationSeconds();
  }

  private Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
  }

  private SecretKey createSigningKey(String secret) {
    byte[] keyBytes =
        secret.startsWith("base64:")
            ? Decoders.BASE64.decode(secret.substring("base64:".length()))
            : secret.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
