package com.revanth.scheduler_api.repository;

import com.revanth.scheduler_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByApiKeyHash(String apiKeyHash);
}
