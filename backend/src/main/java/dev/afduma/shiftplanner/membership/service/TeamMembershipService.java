package dev.afduma.shiftplanner.membership.service;

import dev.afduma.shiftplanner.auth.service.AuthenticatedUser;
import dev.afduma.shiftplanner.common.exception.ConflictException;
import dev.afduma.shiftplanner.common.exception.ResourceNotFoundException;
import dev.afduma.shiftplanner.membership.dto.CreateTeamMembershipRequest;
import dev.afduma.shiftplanner.membership.dto.UpdateTeamMembershipRequest;
import dev.afduma.shiftplanner.membership.model.TeamMembership;
import dev.afduma.shiftplanner.membership.repository.TeamMembershipRepository;
import dev.afduma.shiftplanner.team.model.Team;
import dev.afduma.shiftplanner.team.service.TeamAccessService;
import dev.afduma.shiftplanner.team.service.TeamService;
import dev.afduma.shiftplanner.user.model.User;
import dev.afduma.shiftplanner.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamMembershipService {

  private final TeamMembershipRepository teamMembershipRepository;
  private final TeamService teamService;
  private final UserService userService;
  private final TeamAccessService teamAccessService;

  public TeamMembershipService(
      TeamMembershipRepository teamMembershipRepository,
      TeamService teamService,
      UserService userService,
      TeamAccessService teamAccessService) {
    this.teamMembershipRepository = teamMembershipRepository;
    this.teamService = teamService;
    this.userService = userService;
    this.teamAccessService = teamAccessService;
  }

  @Transactional
  public TeamMembership create(
      AuthenticatedUser authenticatedUser, UUID teamId, CreateTeamMembershipRequest request) {
    Team team = teamService.getById(teamId);
    teamAccessService.requireMembershipManagementAccess(authenticatedUser, teamId);

    User user = userService.getById(request.userId());
    if (teamMembershipRepository.existsByUser_IdAndTeam_Id(request.userId(), teamId)) {
      throw new ConflictException("User is already a member of this team");
    }

    TeamMembership membership = new TeamMembership();
    membership.setTeam(team);
    membership.setUser(user);
    membership.setRole(request.role());
    return teamMembershipRepository.save(membership);
  }

  @Transactional(readOnly = true)
  public List<TeamMembership> getTeamMemberships(
      AuthenticatedUser authenticatedUser, UUID teamId) {
    teamService.getById(teamId);
    teamAccessService.requireViewerAccess(authenticatedUser, teamId);
    return teamMembershipRepository.findAllByTeam_IdOrderByCreatedAtAsc(teamId);
  }

  @Transactional(readOnly = true)
  public List<TeamMembership> getUserMemberships(
      AuthenticatedUser authenticatedUser, UUID userId) {
    teamAccessService.requireAdminOrSelf(authenticatedUser, userId);
    userService.getById(userId);
    return teamMembershipRepository.findAllByUser_IdOrderByCreatedAtAsc(userId);
  }

  @Transactional
  public TeamMembership update(
      AuthenticatedUser authenticatedUser,
      UUID teamId,
      UUID membershipId,
      UpdateTeamMembershipRequest request) {
    teamService.getById(teamId);
    teamAccessService.requireMembershipManagementAccess(authenticatedUser, teamId);

    TeamMembership membership = getByIdAndTeamId(membershipId, teamId);
    membership.setRole(request.role());
    return teamMembershipRepository.save(membership);
  }

  @Transactional
  public void delete(AuthenticatedUser authenticatedUser, UUID teamId, UUID membershipId) {
    teamService.getById(teamId);
    teamAccessService.requireMembershipManagementAccess(authenticatedUser, teamId);

    TeamMembership membership = getByIdAndTeamId(membershipId, teamId);
    teamMembershipRepository.delete(membership);
  }

  @Transactional(readOnly = true)
  public TeamMembership getByIdAndTeamId(UUID membershipId, UUID teamId) {
    return teamMembershipRepository
        .findByIdAndTeam_Id(membershipId, teamId)
        .orElseThrow(() -> new ResourceNotFoundException("Team membership not found"));
  }
}
