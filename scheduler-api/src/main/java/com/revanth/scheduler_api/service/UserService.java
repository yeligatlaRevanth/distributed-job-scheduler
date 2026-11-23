package com.revanth.scheduler_api.service;

import com.revanth.scheduler_api.dto.request.CreateUserRequest;
import com.revanth.scheduler_api.dto.response.CreateUserResponse;
import com.revanth.scheduler_api.entity.User;
import com.revanth.scheduler_api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class UserService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final ApiKeyService apiKeyService;

    public UserService(
            UserRepository userRepository,
            ApiKeyService apiKeyService
    ) {
        this.userRepository = userRepository;
        this.apiKeyService = apiKeyService;
    }

    @Transactional
    public CreateUserResponse createUser(CreateUserRequest request) {

        String apiKey = generateApiKey();
        String apiKeyHash = apiKeyService.hashApiKey(apiKey);
        User user = new User(request.getName(), apiKeyHash);

        User savedUser = userRepository.save(user);

        return new CreateUserResponse(
                savedUser.getId(),
                savedUser.getName(),
                apiKey
        );
    }

    private String generateApiKey() {

        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }
}