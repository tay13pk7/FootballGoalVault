package com.spring.learn.boot.goalvault.repository;

import com.spring.learn.boot.goalvault.entity.MatchReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchReviewRepository extends JpaRepository<MatchReview, Long> {

    Page<MatchReview> findByMatchId(Long matchId, Pageable pageable);

    Page<MatchReview> findByFanId(Long fanId, Pageable pageable);

    // Average review rating for every match where the given team played (home or away).
    // Returns null if there are zero reviews for the team's matches — caller handles the null.
    @Query("select avg(mr.rating) from MatchReview mr " +
            "where mr.match.homeTeam.id = :teamId or mr.match.awayTeam.id = :teamId")
    Double averageRatingForTeam(@Param("teamId") Long teamId);
}
