package dev.afduma.shiftplanner.membership.dto;

import dev.afduma.shiftplanner.membership.model.TeamRole;
import jakarta.validation.constraints.NotNull;

public record UpdateTeamMembershipRequest(@NotNull TeamRole role) {}
