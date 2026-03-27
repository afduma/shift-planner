package dev.afduma.shiftplanner.team.service;

import dev.afduma.shiftplanner.auth.service.AuthenticatedUser;
import dev.afduma.shiftplanner.common.exception.ResourceNotFoundException;
import dev.afduma.shiftplanner.team.dto.CreateTeamRequest;
import dev.afduma.shiftplanner.team.dto.UpdateTeamRequest;
import dev.afduma.shiftplanner.team.model.Team;
import dev.afduma.shiftplanner.team.repository.TeamRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamService {

  private final TeamRepository teamRepository;
  private final TeamAccessService teamAccessService;

  public TeamService(TeamRepository teamRepository, TeamAccessService teamAccessService) {
    this.teamRepository = teamRepository;
    this.teamAccessService = teamAccessService;
  }

  @Transactional
  public Team create(AuthenticatedUser authenticatedUser, CreateTeamRequest request) {
    teamAccessService.requireAdmin(authenticatedUser);

    Team team = new Team();
    applyRequest(team, request.name(), request.description(), request.active());
    return teamRepository.save(team);
  }

  @Transactional(readOnly = true)
  public List<Team> getVisibleTeams(AuthenticatedUser authenticatedUser) {
    List<UUID> visibleTeamIds = teamAccessService.visibleTeamIds(authenticatedUser);
    if (visibleTeamIds.isEmpty()) {
      return teamAccessService.isAdmin(authenticatedUser)
          ? teamRepository.findAllByOrderByNameAsc()
          : List.of();
    }

    return teamRepository.findAllByIdInOrderByNameAsc(visibleTeamIds);
  }

  @Transactional(readOnly = true)
  public Team getVisibleTeamById(AuthenticatedUser authenticatedUser, UUID teamId) {
    Team team = getById(teamId);
    teamAccessService.requireViewerAccess(authenticatedUser, teamId);
    return team;
  }

  @Transactional
  public Team update(AuthenticatedUser authenticatedUser, UUID teamId, UpdateTeamRequest request) {
    teamAccessService.requireAdmin(authenticatedUser);

    Team team = getById(teamId);
    applyRequest(team, request.name(), request.description(), request.active());
    return teamRepository.save(team);
  }

  @Transactional(readOnly = true)
  public Team getById(UUID teamId) {
    return teamRepository
        .findById(teamId)
        .orElseThrow(() -> new ResourceNotFoundException("Team not found"));
  }

  private void applyRequest(Team team, String name, String description, boolean active) {
    team.setName(name.trim());
    team.setDescription(normalizeDescription(description));
    team.setActive(active);
  }

  private String normalizeDescription(String description) {
    if (description == null) {
      return null;
    }

    String normalized = description.trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
