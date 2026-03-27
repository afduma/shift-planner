package dev.afduma.shiftplanner.shift.controller;

import dev.afduma.shiftplanner.auth.service.AuthenticatedUser;
import dev.afduma.shiftplanner.shift.dto.CreateShiftRequest;
import dev.afduma.shiftplanner.shift.dto.ShiftResponse;
import dev.afduma.shiftplanner.shift.dto.UpdateShiftRequest;
import dev.afduma.shiftplanner.shift.mapper.ShiftMapper;
import dev.afduma.shiftplanner.shift.service.ShiftService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

  private final ShiftService shiftService;
  private final ShiftMapper shiftMapper;

  public ShiftController(ShiftService shiftService, ShiftMapper shiftMapper) {
    this.shiftService = shiftService;
    this.shiftMapper = shiftMapper;
  }

  @PostMapping
  public ResponseEntity<ShiftResponse> create(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @Valid @RequestBody CreateShiftRequest request) {
    ShiftResponse response = shiftMapper.toResponse(shiftService.create(authenticatedUser, request));
    return ResponseEntity.created(URI.create("/api/shifts/" + response.id())).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ShiftResponse> getById(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser, @PathVariable UUID id) {
    return ResponseEntity.ok(shiftMapper.toResponse(shiftService.getVisibleById(authenticatedUser, id)));
  }

  @GetMapping
  public ResponseEntity<List<ShiftResponse>> search(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @RequestParam(required = false) UUID teamId,
      @RequestParam(required = false) UUID userId,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to) {
    List<ShiftResponse> response =
        shiftService.search(authenticatedUser, teamId, userId, from, to).stream()
            .map(shiftMapper::toResponse)
            .toList();
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ShiftResponse> update(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateShiftRequest request) {
    return ResponseEntity.ok(shiftMapper.toResponse(shiftService.update(authenticatedUser, id, request)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser, @PathVariable UUID id) {
    shiftService.delete(authenticatedUser, id);
    return ResponseEntity.noContent().build();
  }
}
