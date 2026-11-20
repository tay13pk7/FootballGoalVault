package com.spring.learn.boot.goalvault.dto;

public record CoachResponse(
        Long id,
        String name,
        String nationality,
        Long teamId,
        String teamName
) {}
