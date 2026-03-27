package dev.afduma.shiftplanner.team.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.afduma.shiftplanner.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class TeamControllerIntegrationTest extends IntegrationTest {

  @Test
  void adminCanCreateReadAndUpdateTeam() throws Exception {
    String accessToken = loginAndExtractAccessToken();

    String location =
        mockMvc
            .perform(
                post("/api/teams")
                    .with(csrf())
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "name": "Operations",
                          "description": "Operations scheduling",
                          "active": true
                        }
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Operations"))
            .andExpect(jsonPath("$.description").value("Operations scheduling"))
            .andReturn()
            .getResponse()
            .getHeader("Location");

    mockMvc
        .perform(get("/api/teams").header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Operations"));

    mockMvc
        .perform(get(location).header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Operations"))
        .andExpect(jsonPath("$.active").value(true));

    mockMvc
        .perform(
            put(location)
                .with(csrf())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "Operations Updated",
                      "description": "Updated description",
                      "active": false
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Operations Updated"))
        .andExpect(jsonPath("$.active").value(false));
  }
}
