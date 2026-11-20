package com.spring.learn.boot.goalvault.repository;

import com.spring.learn.boot.goalvault.entity.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    Page<Match> findByHomeTeamIdOrAwayTeamId(Long homeTeamId, Long awayTeamId, Pageable pageable);

    Page<Match> findByStadiumIgnoreCase(String stadium, Pageable pageable);
}
