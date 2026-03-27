package dev.afduma.shiftplanner.shift.dto;

import dev.afduma.shiftplanner.shift.model.ShiftType;
import java.time.Instant;
import java.util.UUID;

public record ShiftResponse(
    UUID id,
    UUID userId,
    UUID teamId,
    Instant startAt,
    Instant endAt,
    ShiftType type,
    String notes,
    Instant createdAt,
    Instant updatedAt) {}
