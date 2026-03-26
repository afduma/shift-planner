package dev.afduma.shiftplanner.auth.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.afduma.shiftplanner.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AuthControllerIntegrationTest extends IntegrationTest {

  @Test
  void loginReturnsBearerTokenForSeededAdmin() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "admin@shiftplanner.local",
                      "password": "admin123"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isString())
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.expiresIn").value(3600));
  }
}
