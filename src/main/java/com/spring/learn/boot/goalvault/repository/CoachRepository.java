package com.spring.learn.boot.goalvault.repository;

import com.spring.learn.boot.goalvault.entity.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoachRepository extends JpaRepository<Coach, Long> {

    Optional<Coach> findByTeamId(Long teamId);
}
