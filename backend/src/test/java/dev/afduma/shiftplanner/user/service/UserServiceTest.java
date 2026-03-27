package dev.afduma.shiftplanner.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.afduma.shiftplanner.auth.service.AuthenticatedUser;
import dev.afduma.shiftplanner.auth.service.UserAuthenticationService;
import dev.afduma.shiftplanner.common.exception.ConflictException;
import dev.afduma.shiftplanner.common.exception.ResourceNotFoundException;
import dev.afduma.shiftplanner.user.dto.CreateUserRequest;
import dev.afduma.shiftplanner.user.model.SystemRole;
import dev.afduma.shiftplanner.user.model.User;
import dev.afduma.shiftplanner.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  private static final AuthenticatedUser ADMIN =
      new AuthenticatedUser(
          UUID.randomUUID(), "admin@shiftplanner.local", "hash", true, "ADMIN");

  private static final AuthenticatedUser USER =
      new AuthenticatedUser(UUID.randomUUID(), "user@shiftplanner.local", "hash", true, "USER");

  @Mock private UserRepository userRepository;
  @Mock private UserAuthenticationService userAuthenticationService;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository, userAuthenticationService);
  }

  @Test
  void createNormalizesEmail() {
    when(userAuthenticationService.normalizeSubject(" Planner@ShiftPlanner.local "))
        .thenReturn("planner@shiftplanner.local");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    User user =
        userService.create(
            ADMIN,
            new CreateUserRequest(
                " Planner@ShiftPlanner.local ", "Plan", "Ner", Boolean.TRUE, SystemRole.USER));

    assertThat(user.getEmail()).isEqualTo("planner@shiftplanner.local");
    verify(userRepository).save(any(User.class));
  }

  @Test
  void createRejectsDuplicateEmail() {
    User existingUser = new User();
    ReflectionTestUtils.setField(existingUser, "id", UUID.randomUUID());

    when(userAuthenticationService.normalizeSubject("planner@shiftplanner.local"))
        .thenReturn("planner@shiftplanner.local");
    when(userRepository.findByEmailIgnoreCase("planner@shiftplanner.local"))
        .thenReturn(Optional.of(existingUser));

    assertThatThrownBy(
            () ->
                userService.create(
                    ADMIN,
                    new CreateUserRequest(
                        "planner@shiftplanner.local",
                        "Plan",
                        "Ner",
                        Boolean.TRUE,
                        SystemRole.USER)))
        .isInstanceOf(ConflictException.class)
        .hasMessage("Email is already in use");
  }

  @Test
  void getVisibleByIdRejectsOtherNonAdminUser() {
    assertThatThrownBy(() -> userService.getVisibleById(USER, UUID.randomUUID()))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessage("Access denied");
  }

  @Test
  void getByIdThrowsWhenMissing() {
    UUID userId = UUID.randomUUID();
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.getById(userId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("User not found");
  }
}
