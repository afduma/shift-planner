package dev.afduma.shiftplanner.seed;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "shiftplanner.seed")
public record SeedProperties(
    boolean enabled,
    @NotBlank String adminPassword,
    @Min(0) int userCount,
    @Min(0) int teamCount) {}
