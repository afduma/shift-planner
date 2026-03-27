package dev.afduma.shiftplanner.shift.dto;

import dev.afduma.shiftplanner.shift.model.ShiftType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record UpdateShiftRequest(
    @NotNull UUID userId,
    @NotNull UUID teamId,
    @NotNull Instant startAt,
    @NotNull Instant endAt,
    @NotNull ShiftType type,
    @Size(max = 4000) String notes) {}
