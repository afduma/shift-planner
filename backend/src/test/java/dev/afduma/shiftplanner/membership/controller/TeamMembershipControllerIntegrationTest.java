package dev.afduma.shiftplanner.membership.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.afduma.shiftplanner.support.IntegrationTest;
import dev.afduma.shiftplanner.team.model.Team;
import dev.afduma.shiftplanner.team.repository.TeamRepository;
import dev.afduma.shiftplanner.user.model.SystemRole;
import dev.afduma.shiftplanner.user.model.User;
import dev.afduma.shiftplanner.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class TeamMembershipControllerIntegrationTest extends IntegrationTest {

  @Autowired private TeamRepository teamRepository;
  @Autowired private UserRepository userRepository;

  @Test
  void adminCanManageMemberships() throws Exception {
    String accessToken = loginAndExtractAccessToken();
    Team team = new Team();
    team.setName("Operations");
    team.setDescription("Operations scheduling");
    team.setActive(true);
    team = teamRepository.save(team);

    User user = new User();
    user.setEmail("member1@shiftplanner.local");
    user.setFirstName("Member");
    user.setLastName("One");
    user.setActive(true);
    user.setSystemRole(SystemRole.USER);
    user = userRepository.save(user);

    String location =
        mockMvc
            .perform(
                post("/api/teams/{teamId}/memberships", team.getId())
                    .with(csrf())
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "userId": "%s",
                          "role": "PLANNER"
                        }
                        """
                            .formatted(user.getId())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").value(user.getId().toString()))
            .andExpect(jsonPath("$.role").value("PLANNER"))
            .andReturn()
            .getResponse()
            .getHeader("Location");

    mockMvc
        .perform(
            get("/api/teams/{teamId}/memberships", team.getId())
                .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].userId").value(user.getId().toString()));

    mockMvc
        .perform(
            get("/api/users/{userId}/memberships", user.getId())
                .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].teamId").value(team.getId().toString()));

    mockMvc
        .perform(
            put(location)
                .with(csrf())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "role": "LEAD"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("LEAD"));

    mockMvc
        .perform(delete(location).with(csrf()).header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            get("/api/teams/{teamId}/memberships", team.getId())
                .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }
}
