package com.revanth.scheduler_api.controller;

import com.revanth.scheduler_api.dto.request.CreateUserRequest;
import com.revanth.scheduler_api.dto.response.CreateUserResponse;
import com.revanth.scheduler_api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final String adminApiKey;

    public AdminController(
            UserService userService,
            @Value("${app.security.admin-api-key}") String adminApiKey
    ) {
        this.userService = userService;
        this.adminApiKey = adminApiKey;
    }

    @PostMapping("/users")
    public ResponseEntity<CreateUserResponse> createUser(
            @RequestHeader(value = "Authorization", required = false)
            String authorization,
            @Valid @RequestBody CreateUserRequest request
    ) {

        if (!isValidAdminKey(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CreateUserResponse response = userService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private boolean isValidAdminKey(String authorization) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return false;
        }

        String providedKey = authorization.substring(7);

        return MessageDigest.isEqual(
                adminApiKey.getBytes(StandardCharsets.UTF_8),
                providedKey.getBytes(StandardCharsets.UTF_8)
        );
    }
}