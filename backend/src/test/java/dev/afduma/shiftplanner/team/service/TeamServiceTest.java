package dev.afduma.shiftplanner.team.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.afduma.shiftplanner.auth.service.AuthenticatedUser;
import dev.afduma.shiftplanner.common.exception.ResourceNotFoundException;
import dev.afduma.shiftplanner.membership.repository.TeamMembershipRepository;
import dev.afduma.shiftplanner.team.dto.CreateTeamRequest;
import dev.afduma.shiftplanner.team.model.Team;
import dev.afduma.shiftplanner.team.repository.TeamRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

  private static final AuthenticatedUser ADMIN =
      new AuthenticatedUser(
          UUID.randomUUID(), "admin@shiftplanner.local", "hash", true, "ADMIN");

  private static final AuthenticatedUser USER =
      new AuthenticatedUser(UUID.randomUUID(), "user@shiftplanner.local", "hash", true, "USER");

  @Mock private TeamRepository teamRepository;
  @Mock private TeamMembershipRepository teamMembershipRepository;

  private TeamService teamService; 

  @BeforeEach
  void setUp() {
    TeamAccessService teamAccessService = new TeamAccessService(teamMembershipRepository);
    teamService = new TeamService(teamRepository, teamAccessService);
  }

  @Test
  void createTrimsNameAndDescription() {
    when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Team team =
        teamService.create(
            ADMIN, new CreateTeamRequest("  Ops  ", "  Operations team  ", Boolean.TRUE));

    assertThat(team.getName()).isEqualTo("Ops");
    assertThat(team.getDescription()).isEqualTo("Operations team");
    assertThat(team.isActive()).isTrue();
    verify(teamRepository).save(any(Team.class));
  }

  @Test
  void createRejectsNonAdmin() {
    assertThatThrownBy(
            () -> teamService.create(USER, new CreateTeamRequest("Ops", null, Boolean.TRUE)))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessage("Access denied");
  }

  @Test
  void getVisibleTeamsReturnsMembershipScopedTeams() {
    UUID firstTeamId = UUID.randomUUID();
    UUID secondTeamId = UUID.randomUUID();
    Team firstTeam = new Team();
    firstTeam.setName("Alpha");
    Team secondTeam = new Team();
    secondTeam.setName("Bravo");

    when(teamMembershipRepository.findTeamIdsByUserId(USER.getUserId()))
        .thenReturn(List.of(firstTeamId, secondTeamId));
    when(teamRepository.findAllByIdInOrderByNameAsc(List.of(firstTeamId, secondTeamId)))
        .thenReturn(List.of(firstTeam, secondTeam));

    List<Team> teams = teamService.getVisibleTeams(USER);

    assertThat(teams).containsExactly(firstTeam, secondTeam);
  }

  @Test
  void getVisibleTeamByIdThrowsWhenMissing() {
    UUID teamId = UUID.randomUUID();
    when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> teamService.getVisibleTeamById(ADMIN, teamId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Team not found");
  }
}
