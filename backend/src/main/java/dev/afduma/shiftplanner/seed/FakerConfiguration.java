package dev.afduma.shiftplanner.seed;

import java.util.Random;
import net.datafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class FakerConfiguration {

  @Bean
  Faker faker() {
    // Faker keeps demo data realistic without mixing sample content into Flyway schema migrations.
    return new Faker(new Random(42));
  }
}
