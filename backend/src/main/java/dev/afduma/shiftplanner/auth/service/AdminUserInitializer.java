package dev.afduma.shiftplanner.auth.service;

import dev.afduma.shiftplanner.auth.model.IdentityProvider;
import dev.afduma.shiftplanner.auth.model.UserIdentity;
import dev.afduma.shiftplanner.auth.repository.UserIdentityRepository;
import dev.afduma.shiftplanner.user.model.SystemRole;
import dev.afduma.shiftplanner.user.model.User;
import dev.afduma.shiftplanner.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
public class AdminUserInitializer implements CommandLineRunner {

  private static final String ADMIN_EMAIL = "admin@shiftplanner.local";

  private final UserRepository userRepository;
  private final UserIdentityRepository userIdentityRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserAuthenticationService userAuthenticationService;

  public AdminUserInitializer(
      UserRepository userRepository,
      UserIdentityRepository userIdentityRepository,
      PasswordEncoder passwordEncoder,
      UserAuthenticationService userAuthenticationService) {
    this.userRepository = userRepository;
    this.userIdentityRepository = userIdentityRepository;
    this.passwordEncoder = passwordEncoder;
    this.userAuthenticationService = userAuthenticationService;
  }

  @Override
  @Transactional
  public void run(String... args) {
    String normalizedEmail = userAuthenticationService.normalizeSubject(ADMIN_EMAIL);
    if (userIdentityRepository
        .findByProviderAndSubject(IdentityProvider.LOCAL, normalizedEmail)
        .isPresent()) {
      return;
    }

    User user =
        userRepository.findByEmailIgnoreCase(normalizedEmail).orElseGet(() -> createAdminUser());

    UserIdentity identity = new UserIdentity();
    identity.setUser(user);
    identity.setProvider(IdentityProvider.LOCAL);
    identity.setSubject(normalizedEmail);
    identity.setPasswordHash(passwordEncoder.encode("admin123"));
    userIdentityRepository.save(identity);
  }

  private User createAdminUser() {
    User user = new User();
    user.setEmail(userAuthenticationService.normalizeSubject(ADMIN_EMAIL));
    user.setFirstName("Admin");
    user.setLastName("User");
    user.setActive(true);
    user.setSystemRole(SystemRole.ADMIN);
    return userRepository.save(user);
  }
}
