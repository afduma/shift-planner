package dev.afduma.shiftplanner.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTeamRequest(
    @NotBlank @Size(max = 150) String name,
    @Size(max = 4000) String description,
    @NotNull Boolean active) {}
