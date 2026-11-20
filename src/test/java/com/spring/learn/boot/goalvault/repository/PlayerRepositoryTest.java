package com.spring.learn.boot.goalvault.repository;

import com.spring.learn.boot.goalvault.entity.Player;
import com.spring.learn.boot.goalvault.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * REPOSITORY-SLICE test — @DataJpaTest wires only JPA + an embedded H2. No web layer, no security.
 * Chosen over @SpringBootTest because we want a fast, focused test of the Specification-based
 * dynamic filter against a real database (H2), without booting the entire app.
 * Transactions auto-rollback after each test to keep the schema clean.
 */
@DataJpaTest
class PlayerRepositoryTest {

    @Autowired private PlayerRepository playerRepo;
    @Autowired private TeamRepository teamRepo;

    @Test
    void specificationsFilterByPositionAndMinGoals() {
        Team team = new Team();
        team.setName("SpecFC");
        team.setCity("Testville");
        team.setFoundedYear(1950);
        team.setStadiumName("Spec Ground");
        team = teamRepo.save(team);

        savePlayer(team, "Forward Alice", "FORWARD", 15);
        savePlayer(team, "Forward Bob",   "FORWARD", 3);
        savePlayer(team, "Keeper Carl",   "GOALKEEPER", 0);

        Specification<Player> spec = PlayerSpecifications.alwaysTrue()
                .and(PlayerSpecifications.hasPositionIgnoreCase("forward"))
                .and(PlayerSpecifications.goalsAtLeast(10));

        Page<Player> page = playerRepo.findAll(spec, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Forward Alice");
    }

    private void savePlayer(Team team, String name, String position, int goals) {
        Player p = new Player();
        p.setName(name);
        p.setPosition(position);
        p.setJerseyNumber(1);
        p.setTeam(team);
        p.setGoalsScored(goals);
        playerRepo.save(p);
    }
}
