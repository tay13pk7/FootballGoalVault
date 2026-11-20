package com.spring.learn.boot.goalvault.service;

import com.spring.learn.boot.goalvault.dto.TeamRequest;
import com.spring.learn.boot.goalvault.dto.TeamResponse;
import com.spring.learn.boot.goalvault.dto.TeamResponseV2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TeamService {
    TeamResponse create(TeamRequest req);
    TeamResponse update(Long id, TeamRequest req);
    void delete(Long id);
    TeamResponse get(Long id);
    // v2 shape: adds playerCount + averageTeamRating derived from match reviews.
    TeamResponseV2 getV2(Long id);
    Page<TeamResponse> list(String city, Pageable pageable);

    // Used to demonstrate @CachePut: always executes, then writes the return value into the cache.
    TeamResponse updateStadium(Long id, String stadiumName);
}
