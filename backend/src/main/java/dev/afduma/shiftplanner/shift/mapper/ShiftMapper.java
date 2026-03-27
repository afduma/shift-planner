package dev.afduma.shiftplanner.shift.mapper;

import dev.afduma.shiftplanner.shift.dto.ShiftResponse;
import dev.afduma.shiftplanner.shift.model.Shift;
import org.springframework.stereotype.Component;

@Component
public class ShiftMapper {

  public ShiftResponse toResponse(Shift shift) {
    return new ShiftResponse(
        shift.getId(),
        shift.getUser().getId(),
        shift.getTeam().getId(),
        shift.getStartAt(),
        shift.getEndAt(),
        shift.getType(),
        shift.getNotes(),
        shift.getCreatedAt(),
        shift.getUpdatedAt());
  }
}
