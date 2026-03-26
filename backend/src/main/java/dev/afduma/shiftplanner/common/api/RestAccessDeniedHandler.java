package dev.afduma.shiftplanner.common.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  public RestAccessDeniedHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException, ServletException {
    writeResponse(response, request.getRequestURI(), HttpStatus.FORBIDDEN, "Access denied");
  }

  private void writeResponse(
      HttpServletResponse response, String path, HttpStatus status, String message)
      throws IOException {
    response.setStatus(status.value());
    response.setContentType("application/json");
    objectMapper.writeValue(
        response.getOutputStream(),
        new ApiErrorResponse(
            Instant.now(), status.value(), status.getReasonPhrase(), message, path, List.of()));
  }
}
