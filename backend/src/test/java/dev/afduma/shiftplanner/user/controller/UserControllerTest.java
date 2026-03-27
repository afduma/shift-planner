package dev.afduma.shiftplanner.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.afduma.shiftplanner.auth.service.AuthenticatedUser;
import dev.afduma.shiftplanner.common.exception.GlobalExceptionHandler;
import dev.afduma.shiftplanner.user.dto.CreateUserRequest;
import dev.afduma.shiftplanner.user.mapper.UserMapper;
import dev.afduma.shiftplanner.user.model.SystemRole;
import dev.afduma.shiftplanner.user.model.User;
import dev.afduma.shiftplanner.user.service.UserService;
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
class UserControllerTest {

  private static final AuthenticatedUser ADMIN =
      new AuthenticatedUser(
          UUID.randomUUID(), "admin@shiftplanner.local", "hash", true, "ADMIN");

  private MockMvc mockMvc;

  @Mock private UserService userService;

  @BeforeEach
  void setUp() {
    UserController userController = new UserController(userService, new UserMapper());
    mockMvc =
        MockMvcBuilders.standaloneSetup(userController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
  }

  @Test
  void createReturnsCreatedResponse() throws Exception {
    UUID userId = UUID.randomUUID();
    User user = new User();
    ReflectionTestUtils.setField(user, "id", userId);
    user.setEmail("planner@shiftplanner.local");
    user.setFirstName("Plan");
    user.setLastName("Ner");
    user.setActive(true);
    user.setSystemRole(SystemRole.USER);

    when(userService.create(any(), any(CreateUserRequest.class))).thenReturn(user);

    mockMvc
        .perform(
            post("/api/users")
                .principal(authenticationFor(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "planner@shiftplanner.local",
                      "firstName": "Plan",
                      "lastName": "Ner",
                      "active": true,
                      "systemRole": "USER"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/users/" + userId))
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.email").value("planner@shiftplanner.local"));
  }

  @Test
  void createRejectsInvalidEmail() throws Exception {
    mockMvc
        .perform(
            post("/api/users")
                .principal(authenticationFor(ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "nope",
                      "firstName": "Plan",
                      "lastName": "Ner",
                      "active": true,
                      "systemRole": "USER"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"));
  }

  private UsernamePasswordAuthenticationToken authenticationFor(
      AuthenticatedUser authenticatedUser) {
    return UsernamePasswordAuthenticationToken.authenticated(
        authenticatedUser, null, authenticatedUser.getAuthorities());
  }
}
