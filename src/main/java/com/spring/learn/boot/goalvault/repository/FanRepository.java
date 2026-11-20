package com.spring.learn.boot.goalvault.repository;

import com.spring.learn.boot.goalvault.entity.Fan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FanRepository extends JpaRepository<Fan, Long> {

    Optional<Fan> findByUsername(String username);
}
