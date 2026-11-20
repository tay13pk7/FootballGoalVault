package com.spring.learn.boot.goalvault.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// LEARNING: A DTO (Data Transfer Object) decouples the wire format from the JPA entity.
// This prevents leaking DB columns/relationships and lets us evolve the API independently.
public record TeamRequest(
        // LEARNING: @NotBlank rejects null, empty, and whitespace-only strings.
        @NotBlank(message = "name must not be blank")
        // LEARNING: @Size enforces min/max length on strings and collections.
        @Size(min = 1, max = 120, message = "name length must be 1-120")
        String name,
        @Size(max = 80) String city,
        // LEARNING: @Min / @Max enforce numeric bounds. Applied to boxed types.
        @NotNull @Min(1850) @Max(2100) Integer foundedYear,
        @Size(max = 120) String stadiumName
) {}
