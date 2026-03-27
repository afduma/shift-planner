package dev.afduma.shiftplanner.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.afduma.shiftplanner.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class UserControllerIntegrationTest extends IntegrationTest {

  @Test
  void meReturnsCurrentUserWhenAuthorized() throws Exception {
    String accessToken = loginAndExtractAccessToken();

    mockMvc
        .perform(get("/api/users/me").header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("admin@shiftplanner.local"))
        .andExpect(jsonPath("$.firstName").value("Admin"))
        .andExpect(jsonPath("$.lastName").value("User"))
        .andExpect(jsonPath("$.systemRole").value("ADMIN"))
        .andExpect(jsonPath("$.active").value(true));
  }

  @Test
  void meReturnsUnauthorizedWithoutToken() throws Exception {
    mockMvc
        .perform(get("/api/users/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Authentication required"));
  }

  @Test
  void adminCanCreateReadAndUpdateUser() throws Exception {
    String accessToken = loginAndExtractAccessToken();

    String location =
        mockMvc
            .perform(
                post("/api/users")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "email": "worker1@shiftplanner.local",
                          "firstName": "Worker",
                          "lastName": "One",
                          "active": true,
                          "systemRole": "USER"
                        }
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("worker1@shiftplanner.local"))
            .andReturn()
            .getResponse()
            .getHeader("Location");

    mockMvc
        .perform(get(location).header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Worker"));

    mockMvc
        .perform(
            put(location)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "worker1-updated@shiftplanner.local",
                      "firstName": "Worker",
                      "lastName": "Updated",
                      "active": false,
                      "systemRole": "USER"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("worker1-updated@shiftplanner.local"))
        .andExpect(jsonPath("$.active").value(false));
  }
}
