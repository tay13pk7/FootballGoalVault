package com.spring.learn.boot.goalvault.dto;

// LEARNING: Header-based API versioning demo. Same URI (GET /api/v1/teams/{id}), different response
// shape based on X-API-Version request header. V2 exposes extra derived fields.
public record TeamResponseV2(
        Long id,
        String name,
        String city,
        Integer foundedYear,
        String stadiumName,
        int playerCount,
        Double averageTeamRating  // avg of MatchReview.rating for matches involving this team
) {}
