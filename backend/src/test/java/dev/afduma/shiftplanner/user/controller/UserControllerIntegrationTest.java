package dev.afduma.shiftplanner.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.afduma.shiftplanner.support.IntegrationTest;
import org.junit.jupiter.api.Test;

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
}
