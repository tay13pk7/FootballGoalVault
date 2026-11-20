package com.spring.learn.boot.goalvault.repository;

import com.spring.learn.boot.goalvault.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// LEARNING: @Repository marks this interface as a persistence bean. It also translates JPA/SQL
// exceptions into Spring's DataAccessException hierarchy. Spring Data JPA actually auto-detects
// JpaRepository subinterfaces even without @Repository, but declaring it makes intent explicit.
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    // LEARNING: Derived query method. Spring Data JPA parses the method name and generates the
    // implementation at runtime — no SQL required. Combined with Pageable, it returns a paged slice.
    Page<Team> findByCityIgnoreCase(String city, Pageable pageable);
}
