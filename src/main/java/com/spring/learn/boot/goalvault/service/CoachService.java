package com.spring.learn.boot.goalvault.service;

import com.spring.learn.boot.goalvault.dto.CoachRequest;
import com.spring.learn.boot.goalvault.dto.CoachResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CoachService {
    CoachResponse create(CoachRequest req);
    CoachResponse update(Long id, CoachRequest req);
    void delete(Long id);
    CoachResponse get(Long id);
    Page<CoachResponse> list(Pageable pageable);
}
