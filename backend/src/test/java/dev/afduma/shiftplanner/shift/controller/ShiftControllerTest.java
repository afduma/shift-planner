package dev.afduma.shiftplanner.shift.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.afduma.shiftplanner.auth.service.AuthenticatedUser;
import dev.afduma.shiftplanner.common.exception.GlobalExceptionHandler;
import dev.afduma.shiftplanner.shift.dto.CreateShiftRequest;
import dev.afduma.shiftplanner.shift.mapper.ShiftMapper;
import dev.afduma.shiftplanner.shift.model.Shift;
import dev.afduma.shiftplanner.shift.model.ShiftType;
import dev.afduma.shiftplanner.shift.service.ShiftService;
import dev.afduma.shiftplanner.team.model.Team;
import dev.afduma.shiftplanner.user.model.User;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ShiftControllerTest {

  private static final AuthenticatedUser ADMIN =
      new AuthenticatedUser(UUID.randomUUID(), "admin@shiftplanner.local", "hash", true, "ADMIN");

  private MockMvc mockMvc;

  @Mock private ShiftService shiftService;

  @BeforeEach
  void setUp() {
    ShiftController shiftController = new ShiftController(shiftService, new ShiftMapper());
    mockMvc =
        MockMvcBuilders.standaloneSetup(shiftController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
  }

  @Test
  void createReturnsCreatedResponse() throws Exception {
    UUID shiftId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID teamId = UUID.randomUUID();
    Shift shift = shift(shiftId, userId, teamId);

    when(shiftService.create(any(), any(CreateShiftRequest.class))).thenReturn(shift);

    mockMvc
        .perform(
            post("/api/shifts")
                .principal(authenticationFor(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "userId": "%s",
                      "teamId": "%s",
                      "startAt": "2026-03-27T08:00:00Z",
                      "endAt": "2026-03-27T16:00:00Z",
                      "type": "REGULAR",
                      "notes": "Day shift"
                    }
                    """
                        .formatted(userId, teamId)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/shifts/" + shiftId))
        .andExpect(jsonPath("$.userId").value(userId.toString()))
        .andExpect(jsonPath("$.teamId").value(teamId.toString()))
        .andExpect(jsonPath("$.type").value("REGULAR"));
  }

  @Test
  void createRejectsMissingStartAt() throws Exception {
    mockMvc
        .perform(
            post("/api/shifts")
                .principal(authenticationFor(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "userId": "%s",
                      "teamId": "%s",
                      "endAt": "2026-03-27T16:00:00Z",
                      "type": "REGULAR"
                    }
                    """
                        .formatted(UUID.randomUUID(), UUID.randomUUID())))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"));
  }

  private Shift shift(UUID shiftId, UUID userId, UUID teamId) {
    Shift shift = new Shift();
    ReflectionTestUtils.setField(shift, "id", shiftId);

    User user = new User();
    ReflectionTestUtils.setField(user, "id", userId);
    shift.setUser(user);

    Team team = new Team();
    ReflectionTestUtils.setField(team, "id", teamId);
    shift.setTeam(team);

    shift.setStartAt(Instant.parse("2026-03-27T08:00:00Z"));
    shift.setEndAt(Instant.parse("2026-03-27T16:00:00Z"));
    shift.setType(ShiftType.REGULAR);
    shift.setNotes("Day shift");
    return shift;
  }

  private UsernamePasswordAuthenticationToken authenticationFor(
      AuthenticatedUser authenticatedUser) {
    return UsernamePasswordAuthenticationToken.authenticated(
        authenticatedUser, null, authenticatedUser.getAuthorities());
  }
}
