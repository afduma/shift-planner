package dev.afduma.shiftplanner.shift.service;

import dev.afduma.shiftplanner.auth.service.AuthenticatedUser;
import dev.afduma.shiftplanner.common.exception.ConflictException;
import dev.afduma.shiftplanner.common.exception.ResourceNotFoundException;
import dev.afduma.shiftplanner.membership.repository.TeamMembershipRepository;
import dev.afduma.shiftplanner.shift.dto.CreateShiftRequest;
import dev.afduma.shiftplanner.shift.dto.UpdateShiftRequest;
import dev.afduma.shiftplanner.shift.model.Shift;
import dev.afduma.shiftplanner.shift.repository.ShiftRepository;
import dev.afduma.shiftplanner.team.model.Team;
import dev.afduma.shiftplanner.team.service.TeamAccessService;
import dev.afduma.shiftplanner.team.service.TeamService;
import dev.afduma.shiftplanner.user.model.User;
import dev.afduma.shiftplanner.user.service.UserService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShiftService {

  private final ShiftRepository shiftRepository;
  private final TeamService teamService;
  private final UserService userService;
  private final TeamAccessService teamAccessService;
  private final TeamMembershipRepository teamMembershipRepository;
  private final ShiftValidationService shiftValidationService;

  public ShiftService(
      ShiftRepository shiftRepository,
      TeamService teamService,
      UserService userService,
      TeamAccessService teamAccessService,
      TeamMembershipRepository teamMembershipRepository,
      ShiftValidationService shiftValidationService) {
    this.shiftRepository = shiftRepository;
    this.teamService = teamService;
    this.userService = userService;
    this.teamAccessService = teamAccessService;
    this.teamMembershipRepository = teamMembershipRepository;
    this.shiftValidationService = shiftValidationService;
  }

  @Transactional
  public Shift create(AuthenticatedUser authenticatedUser, CreateShiftRequest request) {
    Team team = teamService.getById(request.teamId());
    teamAccessService.requireMembershipManagementAccess(authenticatedUser, request.teamId());
    User user = userService.getById(request.userId());
    validateMembership(request.userId(), request.teamId());
    shiftValidationService.validateShiftRange(request.startAt(), request.endAt());
    shiftValidationService.ensureNoOverlap(request.userId(), request.startAt(), request.endAt());

    Shift shift = new Shift();
    applyRequest(
        shift,
        user,
        team,
        request.startAt(),
        request.endAt(),
        request.type(),
        request.notes());
    return shiftRepository.save(shift);
  }

  @Transactional(readOnly = true)
  public Shift getVisibleById(AuthenticatedUser authenticatedUser, UUID shiftId) {
    Shift shift = getById(shiftId);
    teamAccessService.requireViewerAccess(authenticatedUser, shift.getTeam().getId());
    return shift;
  }

  @Transactional(readOnly = true)
  public List<Shift> search(
      AuthenticatedUser authenticatedUser,
      UUID teamId,
      UUID userId,
      Instant fromAt,
      Instant toAt) {
    if (teamId != null) {
      teamService.getById(teamId);
      teamAccessService.requireViewerAccess(authenticatedUser, teamId);
    } else if (!teamAccessService.isAdmin(authenticatedUser)) {
      throw new org.springframework.security.access.AccessDeniedException("Access denied");
    }

    return shiftRepository.search(teamId, userId, fromAt, toAt);
  }

  @Transactional
  public Shift update(AuthenticatedUser authenticatedUser, UUID shiftId, UpdateShiftRequest request) {
    Shift shift = getById(shiftId);
    Team team = teamService.getById(request.teamId());
    teamAccessService.requireMembershipManagementAccess(authenticatedUser, request.teamId());
    User user = userService.getById(request.userId());
    validateMembership(request.userId(), request.teamId());
    shiftValidationService.validateShiftRange(request.startAt(), request.endAt());
    ensureNoOverlapExcludingCurrent(shiftId, request.userId(), request.startAt(), request.endAt());

    applyRequest(
        shift,
        user,
        team,
        request.startAt(),
        request.endAt(),
        request.type(),
        request.notes());
    return shiftRepository.save(shift);
  }

  @Transactional
  public void delete(AuthenticatedUser authenticatedUser, UUID shiftId) {
    Shift shift = getById(shiftId);
    teamAccessService.requireMembershipManagementAccess(authenticatedUser, shift.getTeam().getId());
    shiftRepository.delete(shift);
  }

  @Transactional(readOnly = true)
  public Shift getById(UUID shiftId) {
    return shiftRepository
        .findById(shiftId)
        .orElseThrow(() -> new ResourceNotFoundException("Shift not found"));
  }

  private void validateMembership(UUID userId, UUID teamId) {
    if (!teamMembershipRepository.existsByUser_IdAndTeam_Id(userId, teamId)) {
      throw new ConflictException("User must belong to the team before being scheduled");
    }
  }

  private void ensureNoOverlapExcludingCurrent(
      UUID shiftId, UUID userId, Instant startAt, Instant endAt) {
    if (shiftRepository.existsOverlappingShiftExcludingId(shiftId, userId, startAt, endAt)) {
      throw new ConflictException("Shift overlaps with an existing shift for this user");
    }
  }

  private void applyRequest(
      Shift shift,
      User user,
      Team team,
      Instant startAt,
      Instant endAt,
      dev.afduma.shiftplanner.shift.model.ShiftType type,
      String notes) {
    shift.setUser(user);
    shift.setTeam(team);
    shift.setStartAt(startAt);
    shift.setEndAt(endAt);
    shift.setType(type);
    shift.setNotes(normalizeNotes(notes));
  }

  private String normalizeNotes(String notes) {
    if (notes == null) {
      return null;
    }

    String normalized = notes.trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
