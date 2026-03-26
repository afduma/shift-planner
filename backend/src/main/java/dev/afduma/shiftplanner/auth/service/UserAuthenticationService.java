package dev.afduma.shiftplanner.auth.service;

import dev.afduma.shiftplanner.auth.model.IdentityProvider;
import dev.afduma.shiftplanner.auth.model.UserIdentity;
import dev.afduma.shiftplanner.auth.repository.UserIdentityRepository;
import dev.afduma.shiftplanner.common.exception.ResourceNotFoundException;
import dev.afduma.shiftplanner.user.model.User;
import dev.afduma.shiftplanner.user.repository.UserRepository;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAuthenticationService implements UserDetailsService {

  private final UserIdentityRepository userIdentityRepository;
  private final UserRepository userRepository;

  public UserAuthenticationService(
      UserIdentityRepository userIdentityRepository, UserRepository userRepository) {
    this.userIdentityRepository = userIdentityRepository;
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserIdentity identity =
        userIdentityRepository
            .findByProviderAndSubject(IdentityProvider.LOCAL, normalizeSubject(username))
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return toAuthenticatedUser(identity);
  }

  @Transactional(readOnly = true)
  public AuthenticatedUser loadAuthenticatedUser(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return new AuthenticatedUser(
        user.getId(), user.getEmail(), "", user.isActive(), user.getSystemRole().name());
  }

  private AuthenticatedUser toAuthenticatedUser(UserIdentity identity) {
    User user = identity.getUser();
    return new AuthenticatedUser(
        user.getId(),
        user.getEmail(),
        identity.getPasswordHash(),
        user.isActive(),
        user.getSystemRole().name());
  }

  public String normalizeSubject(String email) {
    return email.trim().toLowerCase();
  }
}
