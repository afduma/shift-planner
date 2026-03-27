package dev.afduma.shiftplanner.membership.mapper;

import dev.afduma.shiftplanner.membership.dto.TeamMembershipResponse;
import dev.afduma.shiftplanner.membership.model.TeamMembership;
import org.springframework.stereotype.Component;

@Component
public class TeamMembershipMapper {

  public TeamMembershipResponse toResponse(TeamMembership membership) {
    return new TeamMembershipResponse(
        membership.getId(),
        membership.getUser().getId(),
        membership.getTeam().getId(),
        membership.getRole(),
        membership.getCreatedAt(),
        membership.getUpdatedAt());
  }
}
