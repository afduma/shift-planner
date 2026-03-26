package dev.afduma.shiftplanner.user.controller;

import dev.afduma.shiftplanner.auth.service.AuthenticatedUser;
import dev.afduma.shiftplanner.user.dto.CurrentUserResponse;
import dev.afduma.shiftplanner.user.mapper.UserMapper;
import dev.afduma.shiftplanner.user.model.User;
import dev.afduma.shiftplanner.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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

  @GetMapping("/me")
  public ResponseEntity<CurrentUserResponse> me(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
    User user = userService.getById(authenticatedUser.getUserId());
    return ResponseEntity.ok(userMapper.toCurrentUserResponse(user));
  }
}
