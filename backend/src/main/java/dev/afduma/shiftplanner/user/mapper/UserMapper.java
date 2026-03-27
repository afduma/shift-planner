package dev.afduma.shiftplanner.user.mapper;

import dev.afduma.shiftplanner.user.dto.UserResponse;
import dev.afduma.shiftplanner.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserResponse toUserResponse(User user) {
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.isActive(),
        user.getSystemRole(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }
}
