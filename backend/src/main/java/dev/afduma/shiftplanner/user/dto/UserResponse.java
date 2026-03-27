package dev.afduma.shiftplanner.user.dto;

import dev.afduma.shiftplanner.user.model.SystemRole;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    boolean active,
    SystemRole systemRole,
    Instant createdAt,
    Instant updatedAt) {}
