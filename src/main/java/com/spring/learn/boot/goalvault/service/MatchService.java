package com.spring.learn.boot.goalvault.service;

import com.spring.learn.boot.goalvault.dto.MatchRequest;
import com.spring.learn.boot.goalvault.dto.MatchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MatchService {
    MatchResponse create(MatchRequest req);
    MatchResponse update(Long id, MatchRequest req);
    void delete(Long id);
    MatchResponse get(Long id);
    Page<MatchResponse> list(Long teamId, String stadium, Pageable pageable);
}
