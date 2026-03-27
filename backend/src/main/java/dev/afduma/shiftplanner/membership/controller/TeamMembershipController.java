package dev.afduma.shiftplanner.membership.controller;

import dev.afduma.shiftplanner.auth.service.AuthenticatedUser;
import dev.afduma.shiftplanner.membership.dto.CreateTeamMembershipRequest;
import dev.afduma.shiftplanner.membership.dto.TeamMembershipResponse;
import dev.afduma.shiftplanner.membership.dto.UpdateTeamMembershipRequest;
import dev.afduma.shiftplanner.membership.mapper.TeamMembershipMapper;
import dev.afduma.shiftplanner.membership.service.TeamMembershipService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class TeamMembershipController {

  private final TeamMembershipService teamMembershipService;
  private final TeamMembershipMapper teamMembershipMapper;

  public TeamMembershipController(
      TeamMembershipService teamMembershipService, TeamMembershipMapper teamMembershipMapper) {
    this.teamMembershipService = teamMembershipService;
    this.teamMembershipMapper = teamMembershipMapper;
  }

  @PostMapping("/api/teams/{teamId}/memberships")
  public ResponseEntity<TeamMembershipResponse> create(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable UUID teamId,
      @Valid @RequestBody CreateTeamMembershipRequest request) {
    TeamMembershipResponse response =
        teamMembershipMapper.toResponse(
            teamMembershipService.create(authenticatedUser, teamId, request));
    return ResponseEntity.created(URI.create("/api/teams/" + teamId + "/memberships/" + response.id()))
        .body(response);
  }

  @GetMapping("/api/teams/{teamId}/memberships")
  public ResponseEntity<List<TeamMembershipResponse>> getTeamMemberships(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser, @PathVariable UUID teamId) {
    List<TeamMembershipResponse> response =
        teamMembershipService.getTeamMemberships(authenticatedUser, teamId).stream()
            .map(teamMembershipMapper::toResponse)
            .toList();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/api/users/{userId}/memberships")
  public ResponseEntity<List<TeamMembershipResponse>> getUserMemberships(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser, @PathVariable UUID userId) {
    List<TeamMembershipResponse> response =
        teamMembershipService.getUserMemberships(authenticatedUser, userId).stream()
            .map(teamMembershipMapper::toResponse)
            .toList();
    return ResponseEntity.ok(response);
  }

  @PutMapping("/api/teams/{teamId}/memberships/{membershipId}")
  public ResponseEntity<TeamMembershipResponse> update(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable UUID teamId,
      @PathVariable UUID membershipId,
      @Valid @RequestBody UpdateTeamMembershipRequest request) {
    return ResponseEntity.ok(
        teamMembershipMapper.toResponse(
            teamMembershipService.update(authenticatedUser, teamId, membershipId, request)));
  }

  @DeleteMapping("/api/teams/{teamId}/memberships/{membershipId}")
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable UUID teamId,
      @PathVariable UUID membershipId) {
    teamMembershipService.delete(authenticatedUser, teamId, membershipId);
    return ResponseEntity.noContent().build();
  }
}
