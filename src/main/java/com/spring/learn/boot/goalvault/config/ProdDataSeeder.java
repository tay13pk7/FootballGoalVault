package com.spring.learn.boot.goalvault.config;

import com.spring.learn.boot.goalvault.entity.Team;
import com.spring.learn.boot.goalvault.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Minimal seeder used when the "prod" profile is active.
 *
 * LEARNING: @Profile("prod") — the opposite bookend to DataSeeder's @Profile("dev").
 * Only one of the two runs, based on --spring.profiles.active. This is the classic pattern
 * for keeping heavy dev-only bootstrap code out of production while still using a single
 * Spring Boot artefact for both environments.
 */
@Component
@Profile("prod")
public class ProdDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProdDataSeeder.class);

    private final TeamRepository teamRepo;

    public ProdDataSeeder(TeamRepository teamRepo) {
        this.teamRepo = teamRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (teamRepo.count() > 0) {
            log.info("Prod seed skipped — data already present.");
            return;
        }
        Team t = new Team();
        t.setName("Placeholder FC");
        t.setCity("Nowhere");
        t.setFoundedYear(2000);
        t.setStadiumName("Placeholder Ground");
        teamRepo.save(t);
        log.info("Prod seed complete: 1 placeholder team inserted.");
    }
}
