package dev.afduma.shiftplanner.auth.service;

import dev.afduma.shiftplanner.auth.dto.LoginRequest;
import dev.afduma.shiftplanner.auth.dto.LoginResponse;
import dev.afduma.shiftplanner.common.exception.UnauthorizedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }

  public LoginResponse login(LoginRequest request) {
    try {
      Authentication authentication =
          authenticationManager.authenticate(
              UsernamePasswordAuthenticationToken.unauthenticated(
                  request.email(), request.password()));
      AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
      String token = jwtService.generateToken(authenticatedUser);
      return new LoginResponse(token, "Bearer", jwtService.getExpirationSeconds());
    } catch (BadCredentialsException | DisabledException exception) {
      throw new UnauthorizedException("Invalid email or password");
    }
  }
}
