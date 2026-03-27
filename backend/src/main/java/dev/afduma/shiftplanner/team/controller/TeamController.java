package dev.afduma.shiftplanner.team.controller;

import dev.afduma.shiftplanner.auth.service.AuthenticatedUser;
import dev.afduma.shiftplanner.team.dto.CreateTeamRequest;
import dev.afduma.shiftplanner.team.dto.TeamResponse;
import dev.afduma.shiftplanner.team.dto.UpdateTeamRequest;
import dev.afduma.shiftplanner.team.mapper.TeamMapper;
import dev.afduma.shiftplanner.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Teams")
@SecurityRequirement(name = "bearerAuth")
public class TeamController {

  private final TeamService teamService;
  private final TeamMapper teamMapper;

  public TeamController(TeamService teamService, TeamMapper teamMapper) {
    this.teamService = teamService;
    this.teamMapper = teamMapper;
  }

  @PostMapping
  @Operation(summary = "Create a team")
  public ResponseEntity<TeamResponse> create(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @RequestBody CreateTeamRequest request) {
    TeamResponse response = teamMapper.toResponse(teamService.create(authenticatedUser, request));
    return ResponseEntity.created(URI.create("/api/teams/" + response.id())).body(response);
  }

  @GetMapping
  @Operation(summary = "List visible teams")
  public ResponseEntity<List<TeamResponse>> getTeams(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
    List<TeamResponse> response =
        teamService.getVisibleTeams(authenticatedUser).stream()
            .map(teamMapper::toResponse)
            .toList();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a team by id")
  public ResponseEntity<TeamResponse> getTeam(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable UUID id) {
    return ResponseEntity.ok(
        teamMapper.toResponse(teamService.getVisibleTeamById(authenticatedUser, id)));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a team")
  public ResponseEntity<TeamResponse> update(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateTeamRequest request) {
    return ResponseEntity.ok(
        teamMapper.toResponse(teamService.update(authenticatedUser, id, request)));
  }
}
