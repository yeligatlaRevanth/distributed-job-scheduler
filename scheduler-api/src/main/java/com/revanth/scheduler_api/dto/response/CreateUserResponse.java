package com.revanth.scheduler_api.dto.response;

public record CreateUserResponse(
        Long userId,
        String name,
        String apiKey
) {
}