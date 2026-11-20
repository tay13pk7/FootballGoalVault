package com.spring.learn.boot.goalvault.service;

import com.spring.learn.boot.goalvault.dto.PlayerRequest;
import com.spring.learn.boot.goalvault.dto.PlayerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlayerService {
    PlayerResponse create(PlayerRequest req);
    PlayerResponse update(Long id, PlayerRequest req);
    void delete(Long id);
    PlayerResponse get(Long id);

    // Dynamic filtering: any subset of (teamId, position, minGoals, q) may be null.
    Page<PlayerResponse> list(Long teamId, String position, Integer minGoals, String q, Pageable pageable);

    PlayerResponse scoreGoal(Long id);
}
