package dev.afduma.shiftplanner.membership.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.afduma.shiftplanner.auth.service.AuthenticatedUser;
import dev.afduma.shiftplanner.common.exception.GlobalExceptionHandler;
import dev.afduma.shiftplanner.membership.dto.CreateTeamMembershipRequest;
import dev.afduma.shiftplanner.membership.mapper.TeamMembershipMapper;
import dev.afduma.shiftplanner.membership.model.TeamMembership;
import dev.afduma.shiftplanner.membership.model.TeamRole;
import dev.afduma.shiftplanner.membership.service.TeamMembershipService;
import dev.afduma.shiftplanner.team.model.Team;
import dev.afduma.shiftplanner.user.model.User;
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
class TeamMembershipControllerTest {

  private static final AuthenticatedUser ADMIN =
      new AuthenticatedUser(
          UUID.randomUUID(), "admin@shiftplanner.local", "hash", true, "ADMIN");

  private MockMvc mockMvc;

  @Mock private TeamMembershipService teamMembershipService;

  @BeforeEach
  void setUp() {
    TeamMembershipController controller =
        new TeamMembershipController(teamMembershipService, new TeamMembershipMapper());
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
  }

  @Test
  void createReturnsCreatedResponse() throws Exception {
    UUID teamId = UUID.randomUUID();
    UUID membershipId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    TeamMembership membership = membership(membershipId, teamId, userId, TeamRole.PLANNER);
    when(teamMembershipService.create(any(), eq(teamId), any(CreateTeamMembershipRequest.class)))
        .thenReturn(membership);

    mockMvc
        .perform(
            post("/api/teams/{teamId}/memberships", teamId)
                .principal(authenticationFor(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "userId": "%s",
                      "role": "PLANNER"
                    }
                    """
                        .formatted(userId)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/teams/" + teamId + "/memberships/" + membershipId))
        .andExpect(jsonPath("$.id").value(membershipId.toString()))
        .andExpect(jsonPath("$.teamId").value(teamId.toString()))
        .andExpect(jsonPath("$.userId").value(userId.toString()))
        .andExpect(jsonPath("$.role").value("PLANNER"));
  }

  @Test
  void createRejectsMissingUserId() throws Exception {
    UUID teamId = UUID.randomUUID();

    mockMvc
        .perform(
            post("/api/teams/{teamId}/memberships", teamId)
                .principal(authenticationFor(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "role": "PLANNER"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.violations[0].field").value("userId"));
  }

  @Test
  void deleteReturnsNoContent() throws Exception {
    UUID teamId = UUID.randomUUID();
    UUID membershipId = UUID.randomUUID();

    mockMvc
        .perform(
            delete("/api/teams/{teamId}/memberships/{membershipId}", teamId, membershipId)
                .principal(authenticationFor(ADMIN)))
        .andExpect(status().isNoContent());
  }

  private TeamMembership membership(UUID membershipId, UUID teamId, UUID userId, TeamRole role) {
    TeamMembership membership = new TeamMembership();
    ReflectionTestUtils.setField(membership, "id", membershipId);

    Team team = new Team();
    ReflectionTestUtils.setField(team, "id", teamId);
    membership.setTeam(team);

    User user = new User();
    ReflectionTestUtils.setField(user, "id", userId);
    membership.setUser(user);
    membership.setRole(role);
    return membership;
  }

  private UsernamePasswordAuthenticationToken authenticationFor(
      AuthenticatedUser authenticatedUser) {
    return UsernamePasswordAuthenticationToken.authenticated(
        authenticatedUser, null, authenticatedUser.getAuthorities());
  }
}
