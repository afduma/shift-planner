package dev.afduma.shiftplanner.team.dto;

import java.time.Instant;
import java.util.UUID;

public record TeamResponse(
    UUID id,
    String name,
    String description,
    boolean active,
    Instant createdAt,
    Instant updatedAt) {}
