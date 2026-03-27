package dev.afduma.shiftplanner.membership.dto;

import dev.afduma.shiftplanner.membership.model.TeamRole;
import java.time.Instant;
import java.util.UUID;

public record TeamMembershipResponse(
    UUID id,
    UUID userId,
    UUID teamId,
    TeamRole role,
    Instant createdAt,
    Instant updatedAt) {}
