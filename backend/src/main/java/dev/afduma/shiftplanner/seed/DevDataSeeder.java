package dev.afduma.shiftplanner.seed;

import dev.afduma.shiftplanner.team.model.Team;
import dev.afduma.shiftplanner.user.model.User;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

  private final SeedProperties seedProperties;
  private final SeedDataGenerator seedDataGenerator;

  public DevDataSeeder(SeedProperties seedProperties, SeedDataGenerator seedDataGenerator) {
    this.seedProperties = seedProperties;
    this.seedDataGenerator = seedDataGenerator;
  }

  @Override
  public void run(String... args) {
    // Flyway owns schema evolution. Runtime demo data stays profile-gated so production stays
    // clean.
    if (!seedProperties.enabled()) {
      log.info("Development seed is disabled. Skipping demo data generation.");
      return;
    }

    log.info(
        "Starting development seed: userCount={}, teamCount={}",
        seedProperties.userCount(),
        seedProperties.teamCount());

    User admin = seedDataGenerator.seedAdmin(seedProperties.adminPassword());
    List<User> demoUsers = seedDataGenerator.seedDemoUsers(seedProperties.userCount());
    List<Team> demoTeams = seedDataGenerator.seedTeams(seedProperties.teamCount());
    int membershipCount = seedDataGenerator.seedMemberships(demoUsers, demoTeams);

    log.info(
        "Development seed completed: admin={}, demoUsers={}, teams={}, membershipsEnsured={}",
        admin.getEmail(),
        demoUsers.size(),
        demoTeams.size(),
        membershipCount);
  }
}
