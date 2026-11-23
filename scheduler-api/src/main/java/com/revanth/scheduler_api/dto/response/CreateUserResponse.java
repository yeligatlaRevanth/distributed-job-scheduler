package com.revanth.scheduler_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateUserResponse {

    private final Long userId;
    private final String name;
    private final String apiKey;
}