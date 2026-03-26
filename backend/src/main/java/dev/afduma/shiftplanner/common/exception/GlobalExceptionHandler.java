package dev.afduma.shiftplanner.common.exception;

import dev.afduma.shiftplanner.common.api.ApiErrorResponse;
import dev.afduma.shiftplanner.common.api.ApiValidationError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiErrorResponse> handleValidation(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    List<ApiValidationError> violations =
        exception.getBindingResult().getFieldErrors().stream()
            .map(this::toValidationError)
            .toList();
    return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, violations);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  ResponseEntity<ApiErrorResponse> handleConstraintViolation(
      ConstraintViolationException exception, HttpServletRequest request) {
    List<ApiValidationError> violations =
        exception.getConstraintViolations().stream()
            .map(
                violation ->
                    new ApiValidationError(
                        violation.getPropertyPath().toString(), violation.getMessage()))
            .toList();
    return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, violations);
  }

  @ExceptionHandler(UnauthorizedException.class)
  ResponseEntity<ApiErrorResponse> handleUnauthorized(
      UnauthorizedException exception, HttpServletRequest request) {
    return buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage(), request, List.of());
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ApiErrorResponse> handleAccessDenied(
      AccessDeniedException exception, HttpServletRequest request) {
    return buildResponse(HttpStatus.FORBIDDEN, "Access denied", request, List.of());
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  ResponseEntity<ApiErrorResponse> handleNotFound(
      ResourceNotFoundException exception, HttpServletRequest request) {
    return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request, List.of());
  }

  @ExceptionHandler(ConflictException.class)
  ResponseEntity<ApiErrorResponse> handleConflict(
      ConflictException exception, HttpServletRequest request) {
    return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request, List.of());
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiErrorResponse> handleUnexpected(
      Exception exception, HttpServletRequest request) {
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request, List.of());
  }

  private ApiValidationError toValidationError(FieldError error) {
    return new ApiValidationError(error.getField(), error.getDefaultMessage());
  }

  private ResponseEntity<ApiErrorResponse> buildResponse(
      HttpStatus status,
      String message,
      HttpServletRequest request,
      List<ApiValidationError> violations) {
    ApiErrorResponse response =
        new ApiErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            request.getRequestURI(),
            violations);
    return ResponseEntity.status(status).body(response);
  }
}
