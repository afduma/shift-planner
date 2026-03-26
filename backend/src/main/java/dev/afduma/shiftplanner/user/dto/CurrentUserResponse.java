package dev.afduma.shiftplanner.user.dto;

import dev.afduma.shiftplanner.user.model.SystemRole;
import java.util.UUID;

public record CurrentUserResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    boolean active,
    SystemRole systemRole) {}
