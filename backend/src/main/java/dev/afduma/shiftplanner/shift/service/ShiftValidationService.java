package dev.afduma.shiftplanner.shift.service;

import dev.afduma.shiftplanner.common.exception.ConflictException;
import dev.afduma.shiftplanner.shift.repository.ShiftRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ShiftValidationService {

  private final ShiftRepository shiftRepository;

  public ShiftValidationService(ShiftRepository shiftRepository) {
    this.shiftRepository = shiftRepository;
  }

  public void validateShiftRange(Instant startAt, Instant endAt) {
    if (!endAt.isAfter(startAt)) {
      throw new ConflictException("Shift end time must be after start time");
    }
  }

  public void ensureNoOverlap(UUID userId, Instant startAt, Instant endAt) {
    if (shiftRepository.existsOverlappingShift(userId, startAt, endAt)) {
      throw new ConflictException("Shift overlaps with an existing shift for this user");
    }
  }
}
