package dev.afduma.shiftplanner.team.service;

import dev.afduma.shiftplanner.auth.service.AuthenticatedUser;
import dev.afduma.shiftplanner.membership.repository.TeamMembershipRepository;
import dev.afduma.shiftplanner.user.model.SystemRole;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class TeamAccessService {

  private final TeamMembershipRepository teamMembershipRepository;

  public TeamAccessService(TeamMembershipRepository teamMembershipRepository) {
    this.teamMembershipRepository = teamMembershipRepository;
  }

  public void requireAdmin(AuthenticatedUser authenticatedUser) {
    if (!isAdmin(authenticatedUser)) {
      throw new AccessDeniedException("Access denied");
    }
  }

  public boolean isAdmin(AuthenticatedUser authenticatedUser) {
    return SystemRole.ADMIN.name().equals(authenticatedUser.getSystemRole());
  }

  public void requireViewerAccess(AuthenticatedUser authenticatedUser, UUID teamId) {
    if (isAdmin(authenticatedUser)) {
      return;
    }

    boolean hasMembership =
        teamMembershipRepository.existsByUser_IdAndTeam_Id(authenticatedUser.getUserId(), teamId);
    if (!hasMembership) {
      throw new AccessDeniedException("Access denied");
    }
  }

  public List<UUID> visibleTeamIds(AuthenticatedUser authenticatedUser) {
    if (isAdmin(authenticatedUser)) {
      return List.of();
    }

    return teamMembershipRepository.findTeamIdsByUserId(authenticatedUser.getUserId());
  }
}
