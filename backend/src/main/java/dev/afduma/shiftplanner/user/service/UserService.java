package dev.afduma.shiftplanner.user.service;

import dev.afduma.shiftplanner.auth.service.AuthenticatedUser;
import dev.afduma.shiftplanner.auth.service.UserAuthenticationService;
import dev.afduma.shiftplanner.common.exception.ConflictException;
import dev.afduma.shiftplanner.common.exception.ResourceNotFoundException;
import dev.afduma.shiftplanner.user.dto.CreateUserRequest;
import dev.afduma.shiftplanner.user.dto.UpdateUserRequest;
import dev.afduma.shiftplanner.user.model.SystemRole;
import dev.afduma.shiftplanner.user.model.User;
import dev.afduma.shiftplanner.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final UserAuthenticationService userAuthenticationService;

  public UserService(
      UserRepository userRepository, UserAuthenticationService userAuthenticationService) {
    this.userRepository = userRepository;
    this.userAuthenticationService = userAuthenticationService;
  }

  @Transactional
  public User create(AuthenticatedUser authenticatedUser, CreateUserRequest request) {
    requireAdmin(authenticatedUser);
    String email = normalizeEmail(request.email());
    ensureEmailAvailable(email, null);

    User user = new User();
    applyRequest(
        user, email, request.firstName(), request.lastName(), request.active(), request.systemRole());
    return userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public List<User> getAll(AuthenticatedUser authenticatedUser) {
    requireAdmin(authenticatedUser);
    return userRepository.findAllByOrderByCreatedAtAsc();
  }

  @Transactional(readOnly = true)
  public User getById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }

  @Transactional(readOnly = true)
  public User getVisibleById(AuthenticatedUser authenticatedUser, UUID userId) {
    requireAdminOrSelf(authenticatedUser, userId);
    return getById(userId);
  }

  @Transactional
  public User update(
      AuthenticatedUser authenticatedUser, UUID userId, UpdateUserRequest request) {
    requireAdmin(authenticatedUser);

    User user = getById(userId);
    String email = normalizeEmail(request.email());
    ensureEmailAvailable(email, userId);
    applyRequest(
        user, email, request.firstName(), request.lastName(), request.active(), request.systemRole());
    return userRepository.save(user);
  }

  @Transactional
  public void delete(AuthenticatedUser authenticatedUser, UUID userId) {
    requireAdmin(authenticatedUser);

    User user = getById(userId);
    try {
      userRepository.delete(user);
      userRepository.flush();
    } catch (DataIntegrityViolationException exception) {
      throw new ConflictException("User cannot be deleted because related records exist");
    }
  }

  private void applyRequest(
      User user,
      String email,
      String firstName,
      String lastName,
      boolean active,
      SystemRole systemRole) {
    user.setEmail(email);
    user.setFirstName(firstName.trim());
    user.setLastName(lastName.trim());
    user.setActive(active);
    user.setSystemRole(systemRole);
  }

  private void ensureEmailAvailable(String email, UUID currentUserId) {
    userRepository
        .findByEmailIgnoreCase(email)
        .filter(existingUser -> !existingUser.getId().equals(currentUserId))
        .ifPresent(existingUser -> {
          throw new ConflictException("Email is already in use");
        });
  }

  private String normalizeEmail(String email) {
    return userAuthenticationService.normalizeSubject(email);
  }

  private void requireAdmin(AuthenticatedUser authenticatedUser) {
    if (!SystemRole.ADMIN.name().equals(authenticatedUser.getSystemRole())) {
      throw new AccessDeniedException("Access denied");
    }
  }

  private void requireAdminOrSelf(AuthenticatedUser authenticatedUser, UUID userId) {
    if (SystemRole.ADMIN.name().equals(authenticatedUser.getSystemRole())
        || authenticatedUser.getUserId().equals(userId)) {
      return;
    }

    throw new AccessDeniedException("Access denied");
  }
}
