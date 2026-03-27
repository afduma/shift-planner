package dev.afduma.shiftplanner.team.mapper;

import dev.afduma.shiftplanner.team.dto.TeamResponse;
import dev.afduma.shiftplanner.team.model.Team;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper {

  public TeamResponse toResponse(Team team) {
    return new TeamResponse(
        team.getId(),
        team.getName(),
        team.getDescription(),
        team.isActive(),
        team.getCreatedAt(),
        team.getUpdatedAt());
  }
}
