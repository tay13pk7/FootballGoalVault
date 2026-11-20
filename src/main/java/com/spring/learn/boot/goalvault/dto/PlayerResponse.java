package com.spring.learn.boot.goalvault.dto;

public record PlayerResponse(
        Long id,
        String name,
        String position,
        Integer jerseyNumber,
        Integer goalsScored,
        Long teamId,
        String teamName
) {}
