package com.spring.learn.boot.goalvault.repository;

import com.spring.learn.boot.goalvault.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// LEARNING: JpaSpecificationExecutor adds findAll(Specification, Pageable) etc — the entry point for
// DYNAMIC filtering built from Criteria API predicates. See PlayerSpecifications for the pieces.
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long>, JpaSpecificationExecutor<Player> {

    Page<Player> findByTeamId(Long teamId, Pageable pageable);

    Page<Player> findByNameContainingIgnoreCase(String q, Pageable pageable);

    // LEARNING: @Query lets us write JPQL (or native SQL with nativeQuery=true) when derived methods aren't enough.
    // :param bindings come from @Param.
    @Query("select p from Player p where p.goalsScored >= :min order by p.goalsScored desc")
    Page<Player> findTopScorers(@Param("min") int minGoals, Pageable pageable);
}
