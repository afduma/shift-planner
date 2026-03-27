package dev.afduma.shiftplanner.shift.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.afduma.shiftplanner.auth.service.AuthenticatedUser;
import dev.afduma.shiftplanner.common.exception.ConflictException;
import dev.afduma.shiftplanner.common.exception.ResourceNotFoundException;
import dev.afduma.shiftplanner.membership.repository.TeamMembershipRepository;
import dev.afduma.shiftplanner.shift.dto.CreateShiftRequest;
import dev.afduma.shiftplanner.shift.dto.UpdateShiftRequest;
import dev.afduma.shiftplanner.shift.model.Shift;
import dev.afduma.shiftplanner.shift.model.ShiftType;
import dev.afduma.shiftplanner.shift.repository.ShiftRepository;
import dev.afduma.shiftplanner.team.model.Team;
import dev.afduma.shiftplanner.team.service.TeamAccessService;
import dev.afduma.shiftplanner.team.service.TeamService;
import dev.afduma.shiftplanner.user.model.User;
import dev.afduma.shiftplanner.user.service.UserService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ShiftServiceTest {

  private static final AuthenticatedUser ADMIN =
      new AuthenticatedUser(UUID.randomUUID(), "admin@shiftplanner.local", "hash", true, "ADMIN");

  @Mock private ShiftRepository shiftRepository;
  @Mock private TeamService teamService;
  @Mock private UserService userService;
  @Mock private TeamAccessService teamAccessService;
  @Mock private TeamMembershipRepository teamMembershipRepository;
  @Mock private ShiftValidationService shiftValidationService;

  private ShiftService shiftService;

  @BeforeEach
  void setUp() {
    shiftService =
        new ShiftService(
            shiftRepository,
            teamService,
            userService,
            teamAccessService,
            teamMembershipRepository,
            shiftValidationService);
  }

  @Test
  void createRejectsUserOutsideTeam() {
    UUID teamId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(teamService.getById(teamId)).thenReturn(team(teamId));
    when(userService.getById(userId)).thenReturn(user(userId));
    when(teamMembershipRepository.existsByUser_IdAndTeam_Id(userId, teamId)).thenReturn(false);

    assertThatThrownBy(
            () ->
                shiftService.create(
                    ADMIN,
                    new CreateShiftRequest(
                        userId,
                        teamId,
                        Instant.parse("2026-03-27T08:00:00Z"),
                        Instant.parse("2026-03-27T16:00:00Z"),
                        ShiftType.REGULAR,
                        "Day shift")))
        .isInstanceOf(ConflictException.class)
        .hasMessage("User must belong to the team before being scheduled");
  }

  @Test
  void createPersistsShift() {
    UUID teamId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Team team = team(teamId);
    User user = user(userId);
    when(teamService.getById(teamId)).thenReturn(team);
    when(userService.getById(userId)).thenReturn(user);
    when(teamMembershipRepository.existsByUser_IdAndTeam_Id(userId, teamId)).thenReturn(true);
    when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Shift shift =
        shiftService.create(
            ADMIN,
            new CreateShiftRequest(
                userId,
                teamId,
                Instant.parse("2026-03-27T08:00:00Z"),
                Instant.parse("2026-03-27T16:00:00Z"),
                ShiftType.REGULAR,
                " Day shift "));

    assertThat(shift.getUser()).isSameAs(user);
    assertThat(shift.getTeam()).isSameAs(team);
    assertThat(shift.getNotes()).isEqualTo("Day shift");
    verify(shiftValidationService)
        .ensureNoOverlap(
            userId, Instant.parse("2026-03-27T08:00:00Z"), Instant.parse("2026-03-27T16:00:00Z"));
  }

  @Test
  void getVisibleByIdThrowsWhenMissing() {
    UUID shiftId = UUID.randomUUID();
    when(shiftRepository.findById(shiftId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> shiftService.getVisibleById(ADMIN, shiftId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Shift not found");
  }

  @Test
  void updateChecksOverlapExcludingCurrentShift() {
    UUID shiftId = UUID.randomUUID();
    UUID teamId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Shift existing = new Shift();
    existing.setTeam(team(teamId));
    when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(existing));
    when(teamService.getById(teamId)).thenReturn(team(teamId));
    when(userService.getById(userId)).thenReturn(user(userId));
    when(teamMembershipRepository.existsByUser_IdAndTeam_Id(userId, teamId)).thenReturn(true);
    when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> invocation.getArgument(0));

    shiftService.update(
        ADMIN,
        shiftId,
        new UpdateShiftRequest(
            userId,
            teamId,
            Instant.parse("2026-03-27T09:00:00Z"),
            Instant.parse("2026-03-27T17:00:00Z"),
            ShiftType.TRAINING,
            null));

    verify(shiftRepository)
        .existsOverlappingShiftExcludingId(
            shiftId,
            userId,
            Instant.parse("2026-03-27T09:00:00Z"),
            Instant.parse("2026-03-27T17:00:00Z"));
  }

  private Team team(UUID teamId) {
    Team team = new Team();
    ReflectionTestUtils.setField(team, "id", teamId);
    return team;
  }

  private User user(UUID userId) {
    User user = new User();
    ReflectionTestUtils.setField(user, "id", userId);
    return user;
  }
}
