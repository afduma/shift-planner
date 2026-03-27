package dev.afduma.shiftplanner.team.controller;

import dev.afduma.shiftplanner.auth.service.AuthenticatedUser;
import dev.afduma.shiftplanner.team.dto.CreateTeamRequest;
import dev.afduma.shiftplanner.team.dto.TeamResponse;
import dev.afduma.shiftplanner.team.dto.UpdateTeamRequest;
import dev.afduma.shiftplanner.team.mapper.TeamMapper;
import dev.afduma.shiftplanner.team.service.TeamService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

  private final TeamService teamService;
  private final TeamMapper teamMapper;

  public TeamController(TeamService teamService, TeamMapper teamMapper) {
    this.teamService = teamService;
    this.teamMapper = teamMapper;
  }

  @PostMapping
  public ResponseEntity<TeamResponse> create(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @RequestBody CreateTeamRequest request) {
    TeamResponse response = teamMapper.toResponse(teamService.create(authenticatedUser, request));
    return ResponseEntity.created(URI.create("/api/teams/" + response.id())).body(response);
  }

  @GetMapping
  public ResponseEntity<List<TeamResponse>> getTeams(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
    List<TeamResponse> response =
        teamService.getVisibleTeams(authenticatedUser).stream()
            .map(teamMapper::toResponse)
            .toList();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<TeamResponse> getTeam(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser, @PathVariable UUID id) {
    return ResponseEntity.ok(
        teamMapper.toResponse(teamService.getVisibleTeamById(authenticatedUser, id)));
  }

  @PutMapping("/{id}")
  public ResponseEntity<TeamResponse> update(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateTeamRequest request) {
    return ResponseEntity.ok(
        teamMapper.toResponse(teamService.update(authenticatedUser, id, request)));
  }
}
