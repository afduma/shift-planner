package dev.afduma.shiftplanner.shift.service;

import dev.afduma.shiftplanner.common.exception.ConflictException;
import dev.afduma.shiftplanner.shift.repository.ShiftRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ShiftValidationService {

  private final ShiftRepository shiftRepository;

  public ShiftValidationService(ShiftRepository shiftRepository) {
    this.shiftRepository = shiftRepository;
  }

  public void validateShiftTimes(LocalTime startTime, LocalTime endTime) {
    if (!endTime.isAfter(startTime)) {
      throw new ConflictException("Shift end time must be after start time");
    }
  }

  public void ensureNoOverlap(
      UUID userId, LocalDate shiftDate, LocalTime startTime, LocalTime endTime) {
    if (shiftRepository.existsOverlappingShift(userId, shiftDate, startTime, endTime)) {
      throw new ConflictException("Shift overlaps with an existing shift for this user");
    }
  }
}
