package com.revanth.scheduler_api.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CreateJobRequest {

        @NotBlank
        @Size(max = 50)
        private String type;

        @NotNull
        private JsonNode payload;

        private LocalDateTime scheduledAt;

        @Min(1)
        @Max(10)
        private Short priority;

        @Min(1)
        @Max(20)
        private Integer maxAttempts;

        @Min(1)
        @Max(3600)
        private Integer timeoutSeconds;
}