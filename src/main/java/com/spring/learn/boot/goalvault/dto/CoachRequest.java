package com.spring.learn.boot.goalvault.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CoachRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull Long teamId,
        @Size(max = 60) String nationality
) {}
