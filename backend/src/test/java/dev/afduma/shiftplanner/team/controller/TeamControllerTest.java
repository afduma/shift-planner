package dev.afduma.shiftplanner.team.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.afduma.shiftplanner.auth.service.AuthenticatedUser;
import dev.afduma.shiftplanner.common.exception.GlobalExceptionHandler;
import dev.afduma.shiftplanner.team.dto.CreateTeamRequest;
import dev.afduma.shiftplanner.team.mapper.TeamMapper;
import dev.afduma.shiftplanner.team.model.Team;
import dev.afduma.shiftplanner.team.service.TeamService;
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
class TeamControllerTest {

  private static final AuthenticatedUser ADMIN =
      new AuthenticatedUser(
          UUID.randomUUID(), "admin@shiftplanner.local", "hash", true, "ADMIN");

  private MockMvc mockMvc;

  @Mock private TeamService teamService;

  @BeforeEach
  void setUp() {
    TeamController teamController = new TeamController(teamService, new TeamMapper());
    mockMvc =
        MockMvcBuilders.standaloneSetup(teamController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
  }

  @Test
  void createReturnsCreatedResponse() throws Exception {
    UUID teamId = UUID.randomUUID();
    Team team = new Team();
    ReflectionTestUtils.setField(team, "id", teamId);
    team.setName("Ops");
    team.setDescription("Operations team");
    team.setActive(true);

    when(teamService.create(any(), any(CreateTeamRequest.class))).thenReturn(team);

    mockMvc
        .perform(
            post("/api/teams")
                .principal(authenticationFor(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "Ops",
                      "description": "Operations team",
                      "active": true
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/teams/" + teamId))
        .andExpect(jsonPath("$.id").value(teamId.toString()))
        .andExpect(jsonPath("$.name").value("Ops"))
        .andExpect(jsonPath("$.description").value("Operations team"))
        .andExpect(jsonPath("$.active").value(true));
  }

  @Test
  void createRejectsBlankName() throws Exception {
    mockMvc
        .perform(
            post("/api/teams")
                .principal(authenticationFor(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "  ",
                      "description": "Operations team",
                      "active": true
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.violations[0].field").value("name"));
  }

  private UsernamePasswordAuthenticationToken authenticationFor(
      AuthenticatedUser authenticatedUser) {
    return UsernamePasswordAuthenticationToken.authenticated(
        authenticatedUser, null, authenticatedUser.getAuthorities());
  }
}
