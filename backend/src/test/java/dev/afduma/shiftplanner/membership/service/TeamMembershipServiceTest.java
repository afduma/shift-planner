package dev.afduma.shiftplanner.membership.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.afduma.shiftplanner.auth.service.AuthenticatedUser;
import dev.afduma.shiftplanner.common.exception.ConflictException;
import dev.afduma.shiftplanner.common.exception.ResourceNotFoundException;
import dev.afduma.shiftplanner.membership.dto.CreateTeamMembershipRequest;
import dev.afduma.shiftplanner.membership.dto.UpdateTeamMembershipRequest;
import dev.afduma.shiftplanner.membership.model.TeamMembership;
import dev.afduma.shiftplanner.membership.model.TeamRole;
import dev.afduma.shiftplanner.membership.repository.TeamMembershipRepository;
import dev.afduma.shiftplanner.team.model.Team;
import dev.afduma.shiftplanner.team.service.TeamAccessService;
import dev.afduma.shiftplanner.team.service.TeamService;
import dev.afduma.shiftplanner.user.model.User;
import dev.afduma.shiftplanner.user.service.UserService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeamMembershipServiceTest {

  private static final AuthenticatedUser ADMIN =
      new AuthenticatedUser(
          UUID.randomUUID(), "admin@shiftplanner.local", "hash", true, "ADMIN");

  @Mock private TeamMembershipRepository teamMembershipRepository;
  @Mock private TeamService teamService;
  @Mock private UserService userService;
  @Mock private TeamAccessService teamAccessService;

  private TeamMembershipService teamMembershipService;

  @BeforeEach
  void setUp() {
    teamMembershipService =
        new TeamMembershipService(
            teamMembershipRepository, teamService, userService, teamAccessService);
  }

  @Test
  void createRejectsDuplicateMembership() {
    UUID teamId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    when(teamService.getById(teamId)).thenReturn(team(teamId));
    when(userService.getById(userId)).thenReturn(user(userId));
    when(teamMembershipRepository.existsByUser_IdAndTeam_Id(userId, teamId)).thenReturn(true);

    assertThatThrownBy(
            () ->
                teamMembershipService.create(
                    ADMIN, teamId, new CreateTeamMembershipRequest(userId, TeamRole.MEMBER)))
        .isInstanceOf(ConflictException.class)
        .hasMessage("User is already a member of this team");

    verify(teamMembershipRepository, never()).save(any(TeamMembership.class));
  }

  @Test
  void createPersistsMembership() {
    UUID teamId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Team team = team(teamId);
    User user = user(userId);

    when(teamService.getById(teamId)).thenReturn(team);
    when(userService.getById(userId)).thenReturn(user);
    when(teamMembershipRepository.save(any(TeamMembership.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TeamMembership membership =
        teamMembershipService.create(
            ADMIN, teamId, new CreateTeamMembershipRequest(userId, TeamRole.PLANNER));

    assertThat(membership.getTeam()).isSameAs(team);
    assertThat(membership.getUser()).isSameAs(user);
    assertThat(membership.getRole()).isEqualTo(TeamRole.PLANNER);
    verify(teamAccessService).requireMembershipManagementAccess(ADMIN, teamId);
  }

  @Test
  void getTeamMembershipsRequiresViewerAccess() {
    UUID teamId = UUID.randomUUID();
    TeamMembership membership = new TeamMembership();

    when(teamService.getById(teamId)).thenReturn(team(teamId));
    when(teamMembershipRepository.findAllByTeam_IdOrderByCreatedAtAsc(teamId))
        .thenReturn(List.of(membership));

    List<TeamMembership> memberships = teamMembershipService.getTeamMemberships(ADMIN, teamId);

    assertThat(memberships).containsExactly(membership);
    verify(teamAccessService).requireViewerAccess(ADMIN, teamId);
  }

  @Test
  void updateThrowsWhenMembershipDoesNotExist() {
    UUID teamId = UUID.randomUUID();
    UUID membershipId = UUID.randomUUID();

    when(teamService.getById(teamId)).thenReturn(team(teamId));
    when(teamMembershipRepository.findByIdAndTeam_Id(membershipId, teamId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                teamMembershipService.update(
                    ADMIN,
                    teamId,
                    membershipId,
                    new UpdateTeamMembershipRequest(TeamRole.LEAD)))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Team membership not found");
  }

  private Team team(UUID teamId) {
    Team team = new Team();
    org.springframework.test.util.ReflectionTestUtils.setField(team, "id", teamId);
    return team;
  }

  private User user(UUID userId) {
    User user = new User();
    org.springframework.test.util.ReflectionTestUtils.setField(user, "id", userId);
    return user;
  }
}
