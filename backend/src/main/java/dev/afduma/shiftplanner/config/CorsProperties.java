package dev.afduma.shiftplanner.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "shiftplanner.cors")
public record CorsProperties(List<String> allowedOrigins) {}
