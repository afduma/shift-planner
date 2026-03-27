package dev.afduma.shiftplanner.membership.dto;

import dev.afduma.shiftplanner.membership.model.TeamRole;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateTeamMembershipRequest(@NotNull UUID userId, @NotNull TeamRole role) {}
