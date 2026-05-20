package dev.afduma.shiftplanner.seed;

import dev.afduma.shiftplanner.auth.model.IdentityProvider;
import dev.afduma.shiftplanner.auth.model.UserIdentity;
import dev.afduma.shiftplanner.auth.repository.UserIdentityRepository;
import dev.afduma.shiftplanner.auth.service.UserAuthenticationService;
import dev.afduma.shiftplanner.membership.model.TeamMembership;
import dev.afduma.shiftplanner.membership.model.TeamRole;
import dev.afduma.shiftplanner.membership.repository.TeamMembershipRepository;
import dev.afduma.shiftplanner.team.model.Team;
import dev.afduma.shiftplanner.team.repository.TeamRepository;
import dev.afduma.shiftplanner.user.model.SystemRole;
import dev.afduma.shiftplanner.user.model.User;
import dev.afduma.shiftplanner.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
public class SeedDataGenerator {

  private static final Logger log = LoggerFactory.getLogger(SeedDataGenerator.class);
  private static final String ADMIN_EMAIL = "admin@shiftplanner.local";
  private static final String DEMO_PASSWORD = "password123";
  private static final int PLANNER_COUNT = 2;
  private static final List<String> DEFAULT_TEAM_NAMES =
      List.of("Engineering", "Support", "Logistics", "Sales", "Operations", "Customer Success");

  private final UserRepository userRepository;
  private final UserIdentityRepository userIdentityRepository;
  private final TeamRepository teamRepository;
  private final TeamMembershipRepository teamMembershipRepository;
  private final UserAuthenticationService userAuthenticationService;
  private final PasswordEncoder passwordEncoder;
  private final Faker faker;

  public SeedDataGenerator(
      UserRepository userRepository,
      UserIdentityRepository userIdentityRepository,
      TeamRepository teamRepository,
      TeamMembershipRepository teamMembershipRepository,
      UserAuthenticationService userAuthenticationService,
      PasswordEncoder passwordEncoder,
      Faker faker) {
    this.userRepository = userRepository;
    this.userIdentityRepository = userIdentityRepository;
    this.teamRepository = teamRepository;
    this.teamMembershipRepository = teamMembershipRepository;
    this.userAuthenticationService = userAuthenticationService;
    this.passwordEncoder = passwordEncoder;
    this.faker = faker;
  }

  @Transactional
  public User seedAdmin(String adminPassword) {
    String normalizedEmail = normalizeEmail(ADMIN_EMAIL);
    boolean adminExists = userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent();
    User admin =
        userRepository.findByEmailIgnoreCase(normalizedEmail).orElseGet(() -> createAdminUser(normalizedEmail));

    admin.setFirstName("Admin");
    admin.setLastName("User");
    admin.setActive(true);
    admin.setSystemRole(SystemRole.ADMIN);
    User savedAdmin = userRepository.save(admin);
    upsertLocalIdentity(savedAdmin, normalizedEmail, adminPassword);
    log.info("Seeded admin user: email={}, created={}", savedAdmin.getEmail(), !adminExists);
    return savedAdmin;
  }

  @Transactional
  public List<User> seedDemoUsers(int userCount) {
    List<User> users = new ArrayList<>();
    int createdCount = 0;

    for (int index = 0; index < userCount; index++) {
      DemoUserProfile profile = nextDemoUserProfile(index);
      boolean existed = userRepository.findByEmailIgnoreCase(profile.email()).isPresent();
      User user =
          userRepository.findByEmailIgnoreCase(profile.email()).orElseGet(() -> createDemoUser(profile));
      upsertLocalIdentity(user, profile.email(), DEMO_PASSWORD);
      users.add(user);
      if (!existed) {
        createdCount++;
        log.debug("Created demo user: {} {} <{}>", user.getFirstName(), user.getLastName(), user.getEmail());
      }
    }

    log.info("Seeded demo users: requested={}, ensured={}, created={}", userCount, users.size(), createdCount);
    return users;
  }

  @Transactional
  public List<Team> seedTeams(int teamCount) {
    List<Team> teams = new ArrayList<>();
    int createdCount = 0;

    for (int index = 0; index < teamCount; index++) {
      DemoTeamProfile profile = nextDemoTeamProfile(index);
      boolean existed = teamRepository.findByNameIgnoreCase(profile.name()).isPresent();
      Team team = teamRepository.findByNameIgnoreCase(profile.name()).orElseGet(() -> createTeam(profile));
      if (team.getDescription() == null || team.getDescription().isBlank()) {
        team.setDescription(profile.description());
      }
      team.setActive(true);
      teams.add(teamRepository.save(team));
      if (!existed) {
        createdCount++;
        log.debug("Created demo team: {}", team.getName());
      }
    }

    log.info("Seeded demo teams: requested={}, ensured={}, created={}", teamCount, teams.size(), createdCount);
    return teams;
  }

  @Transactional
  public int seedMemberships(List<User> users, List<Team> teams) {
    if (users.isEmpty() || teams.isEmpty()) {
      log.info("Skipping membership seed because users or teams are empty.");
      return 0;
    }

    List<User> planners = users.subList(0, Math.min(PLANNER_COUNT, users.size()));
    int membershipCount = 0;

    for (int teamIndex = 0; teamIndex < teams.size(); teamIndex++) {
      Team team = teams.get(teamIndex);
      User lead = planners.get(teamIndex % planners.size());
      membershipCount += ensureMembership(lead, team, TeamRole.LEAD);

      if (planners.size() > 1) {
        User planner = planners.get((teamIndex + 1) % planners.size());
        membershipCount += ensureMembership(planner, team, planner.equals(lead) ? TeamRole.LEAD : TeamRole.PLANNER);
      }
    }

    for (int userIndex = 0; userIndex < users.size(); userIndex++) {
      User user = users.get(userIndex);
      Team primaryTeam = teams.get(userIndex % teams.size());
      membershipCount +=
          ensureMembership(user, primaryTeam, planners.contains(user) ? TeamRole.PLANNER : TeamRole.MEMBER);

      if (teams.size() > 1 && userIndex % 3 == 0) {
        Team secondaryTeam = teams.get((userIndex + 1) % teams.size());
        membershipCount += ensureMembership(user, secondaryTeam, TeamRole.MEMBER);
      }
    }

    log.info("Seeded team memberships: ensured={}", membershipCount);
    return membershipCount;
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

  private User createDemoUser(DemoUserProfile profile) {
    User user = new User();
    user.setEmail(profile.email());
    user.setFirstName(profile.firstName());
    user.setLastName(profile.lastName());
    user.setActive(true);
    user.setSystemRole(SystemRole.USER);
    return userRepository.save(user);
  }

  private Team createTeam(DemoTeamProfile profile) {
    Team team = new Team();
    team.setName(profile.name());
    team.setDescription(profile.description());
    team.setActive(true);
    return teamRepository.save(team);
  }

  private void upsertLocalIdentity(User user, String normalizedEmail, String rawPassword) {
    UserIdentity identity =
        userIdentityRepository
            .findByProviderAndSubject(IdentityProvider.LOCAL, normalizedEmail)
            .orElseGet(UserIdentity::new);
    identity.setUser(user);
    identity.setProvider(IdentityProvider.LOCAL);
    identity.setSubject(normalizedEmail);
    identity.setPasswordHash(passwordEncoder.encode(rawPassword));
    userIdentityRepository.save(identity);
  }

  private int ensureMembership(User user, Team team, TeamRole role) {
    TeamMembership membership =
        teamMembershipRepository
            .findByUser_IdAndTeam_Id(user.getId(), team.getId())
            .orElseGet(() -> createMembership(user, team, role));

    if (membership.getRole() != role) {
      membership.setRole(role);
      teamMembershipRepository.save(membership);
    }

    return 1;
  }

  private TeamMembership createMembership(User user, Team team, TeamRole role) {
    TeamMembership membership = new TeamMembership();
    membership.setUser(user);
    membership.setTeam(team);
    membership.setRole(role);
    return teamMembershipRepository.save(membership);
  }

  private DemoUserProfile nextDemoUserProfile(int index) {
    String firstName = faker.name().firstName();
    String lastName = faker.name().lastName();
    String localPart = normalizeSlug(firstName + "." + lastName + "." + (index + 1));
    String email = normalizeEmail(localPart + "@demo.shiftplanner.local");
    return new DemoUserProfile(firstName, lastName, email);
  }

  private DemoTeamProfile nextDemoTeamProfile(int index) {
    String teamName = index < DEFAULT_TEAM_NAMES.size() ? DEFAULT_TEAM_NAMES.get(index) : faker.company().industry();
    String city = faker.address().cityName();
    String description = "Handles " + faker.company().profession() + " workflows for " + city + '.';
    return new DemoTeamProfile(teamName, description);
  }

  private String normalizeEmail(String email) {
    return userAuthenticationService.normalizeSubject(email);
  }

  private String normalizeSlug(String value) {
    return value
        .toLowerCase()
        .replaceAll("[^a-z0-9]+", ".")
        .replaceAll("(^\\.|\\.$)", "");
  }

  private record DemoUserProfile(String firstName, String lastName, String email) {}

  private record DemoTeamProfile(String name, String description) {}
}
