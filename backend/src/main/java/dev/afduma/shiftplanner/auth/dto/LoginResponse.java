package dev.afduma.shiftplanner.auth.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {}
