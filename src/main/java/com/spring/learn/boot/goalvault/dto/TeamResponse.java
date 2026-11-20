package com.spring.learn.boot.goalvault.dto;

// LEARNING: HATEOAS was intentionally skipped for this learning project. A full HATEOAS pass would
// add spring-hateoas and make this DTO extend RepresentationModel<TeamResponse> — but Java records
// cannot extend classes, so we'd either have to convert TeamResponse to a regular class or wrap it
// in EntityModel<TeamResponse> at the controller. Both would ripple through service/tests without
// adding new Spring concepts on top of what's already demonstrated. If we adopted it, the links
// we'd surface here would be:
//   - self          : GET  /api/v1/teams/{id}
//   - team-players  : GET  /api/v1/players?teamId={id}
//   - team-coach    : GET  /api/v1/coaches?teamId={id}  (or /coaches/{coachId})
// This lets clients navigate the API without hard-coding URLs — the core HATEOAS value proposition.
public record TeamResponse(
        Long id,
        String name,
        String city,
        Integer foundedYear,
        String stadiumName,
        int playerCount
) {}
