package dev.afduma.shiftplanner.support;

import dev.afduma.shiftplanner.auth.model.IdentityProvider;
import dev.afduma.shiftplanner.auth.model.UserIdentity;
import dev.afduma.shiftplanner.auth.repository.UserIdentityRepository;
import dev.afduma.shiftplanner.auth.service.UserAuthenticationService;
import dev.afduma.shiftplanner.user.model.SystemRole;
import dev.afduma.shiftplanner.user.model.User;
import dev.afduma.shiftplanner.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("test")
public class TestDataInitializer implements CommandLineRunner {

  private static final String ADMIN_EMAIL = "admin@shiftplanner.local";
  private static final String ADMIN_PASSWORD = "admin123";

  private final UserRepository userRepository;
  private final UserIdentityRepository userIdentityRepository;
  private final UserAuthenticationService userAuthenticationService;
  private final PasswordEncoder passwordEncoder;

  public TestDataInitializer(
      UserRepository userRepository,
      UserIdentityRepository userIdentityRepository,
      UserAuthenticationService userAuthenticationService,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.userIdentityRepository = userIdentityRepository;
    this.userAuthenticationService = userAuthenticationService;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public void run(String... args) {
    String normalizedEmail = userAuthenticationService.normalizeSubject(ADMIN_EMAIL);
    User admin =
        userRepository.findByEmailIgnoreCase(normalizedEmail).orElseGet(() -> createAdminUser(normalizedEmail));

    UserIdentity identity =
        userIdentityRepository
            .findByProviderAndSubject(IdentityProvider.LOCAL, normalizedEmail)
            .orElseGet(UserIdentity::new);
    identity.setUser(admin);
    identity.setProvider(IdentityProvider.LOCAL);
    identity.setSubject(normalizedEmail);
    identity.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
    userIdentityRepository.save(identity);
  }

  private User createAdminUser(String normalizedEmail) {
    User user = new User();
    user.setEmail(normalizedEmail);
    user.setFirstName("Admin");
    user.setLastName("User");
    user.setActive(true);
    user.setSystemRole(SystemRole.ADMIN);
    return userRepository.save(user);
  }
}
