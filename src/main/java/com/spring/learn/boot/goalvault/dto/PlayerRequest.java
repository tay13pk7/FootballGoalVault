package com.spring.learn.boot.goalvault.dto;

import com.spring.learn.boot.goalvault.validation.ValidPosition;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record PlayerRequest(
        @NotBlank @Size(max = 120) String name,
        // LEARNING: @ValidPosition is our CUSTOM Bean Validation constraint. See validation/ValidPosition.java.
        // Accepts null? No — combined with the length check below via @Size (position must fit).
        @ValidPosition @Size(max = 40) String position,
        // LEARNING: @Positive rejects zero and negatives; use @PositiveOrZero to allow zero.
        @NotNull @Positive @Min(1) Integer jerseyNumber,
        @NotNull Long teamId,
        @NotNull @PositiveOrZero Integer goalsScored
) {}

