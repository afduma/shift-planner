package dev.afduma.shiftplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ShiftplannerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ShiftplannerApplication.class, args);
  }
}
