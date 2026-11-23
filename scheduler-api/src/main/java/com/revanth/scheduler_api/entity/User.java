
package com.revanth.scheduler_api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(
            name = "api_key_hash",
            nullable = false,
            length = 64,
            unique = true,
            columnDefinition = "CHAR(64)"
    )
    private String apiKeyHash;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    protected User() {
    }

    public User(String name, String apiKeyHash) {
        this.name = name;
        this.apiKeyHash = apiKeyHash;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getApiKeyHash() {
        return apiKeyHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
