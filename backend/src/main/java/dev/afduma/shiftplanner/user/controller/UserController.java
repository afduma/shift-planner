package dev.afduma.shiftplanner.user.controller;

import dev.afduma.shiftplanner.auth.service.AuthenticatedUser;
import dev.afduma.shiftplanner.user.dto.CreateUserRequest;
import dev.afduma.shiftplanner.user.dto.UpdateUserRequest;
import dev.afduma.shiftplanner.user.dto.UserResponse;
import dev.afduma.shiftplanner.user.mapper.UserMapper;
import dev.afduma.shiftplanner.user.model.User;
import dev.afduma.shiftplanner.user.service.UserService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;
  private final UserMapper userMapper;

  public UserController(UserService userService, UserMapper userMapper) {
    this.userService = userService;
    this.userMapper = userMapper;
  }

  @PostMapping
  public ResponseEntity<UserResponse> create(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @RequestBody CreateUserRequest request) {
    UserResponse response = userMapper.toUserResponse(userService.create(authenticatedUser, request));
    return ResponseEntity.created(URI.create("/api/users/" + response.id())).body(response);
  }

  @GetMapping
  public ResponseEntity<List<UserResponse>> getUsers(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
    List<UserResponse> response =
        userService.getAll(authenticatedUser).stream().map(userMapper::toUserResponse).toList();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getById(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser, @PathVariable UUID id) {
    return ResponseEntity.ok(userMapper.toUserResponse(userService.getVisibleById(authenticatedUser, id)));
  }

  @GetMapping("/me")
  public ResponseEntity<UserResponse> me(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
    User user = userService.getById(authenticatedUser.getUserId());
    return ResponseEntity.ok(userMapper.toUserResponse(user));
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserResponse> update(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateUserRequest request) {
    return ResponseEntity.ok(userMapper.toUserResponse(userService.update(authenticatedUser, id, request)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser, @PathVariable UUID id) {
    userService.delete(authenticatedUser, id);
    return ResponseEntity.noContent().build();
  }
}
