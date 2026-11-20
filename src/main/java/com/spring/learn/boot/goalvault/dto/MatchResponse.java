package com.spring.learn.boot.goalvault.dto;

import java.time.LocalDateTime;

public record MatchResponse(
        Long id,
        Long homeTeamId,
        String homeTeamName,
        Long awayTeamId,
        String awayTeamName,
        LocalDateTime matchDate,
        Integer homeScore,
        Integer awayScore,
        String stadium
) {}
