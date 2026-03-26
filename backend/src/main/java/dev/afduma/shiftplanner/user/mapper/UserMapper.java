package dev.afduma.shiftplanner.user.mapper;

import dev.afduma.shiftplanner.user.dto.CurrentUserResponse;
import dev.afduma.shiftplanner.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public CurrentUserResponse toCurrentUserResponse(User user) {
    return new CurrentUserResponse(
        user.getId(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.isActive(),
        user.getSystemRole());
  }
}
