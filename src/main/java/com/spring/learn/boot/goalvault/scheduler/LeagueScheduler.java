package com.spring.learn.boot.goalvault.scheduler;

import com.spring.learn.boot.goalvault.repository.MatchRepository;
import com.spring.learn.boot.goalvault.repository.PlayerRepository;
import com.spring.learn.boot.goalvault.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled housekeeping jobs.
 *
 * LEARNING — @Scheduled trigger types:
 *   fixedRate = N   — invoke every N ms measured from previous START (may overlap if job is slow).
 *   fixedDelay = N  — invoke N ms after the previous FINISH (never overlaps; drifts with duration).
 *   cron = "..."    — Unix-style cron with seconds (Spring's cron parser). Full calendar control:
 *                     "0 0 * * * *" = top of every hour, "0 0/15 9-17 * * MON-FRI" = business hours.
 *
 * By default all @Scheduled methods share a single thread. If you need parallel execution,
 * define a TaskScheduler @Bean with a ThreadPoolTaskScheduler.
 */
@Component
public class LeagueScheduler {

    private static final Logger log = LoggerFactory.getLogger(LeagueScheduler.class);

    private final TeamRepository teamRepo;
    private final PlayerRepository playerRepo;
    private final MatchRepository matchRepo;

    public LeagueScheduler(TeamRepository teamRepo, PlayerRepository playerRepo, MatchRepository matchRepo) {
        this.teamRepo = teamRepo;
        this.playerRepo = playerRepo;
        this.matchRepo = matchRepo;
    }

    // Runs every 60 seconds. initialDelay pushes the first run so it doesn't fire during startup.
    @Scheduled(fixedRate = 60_000, initialDelay = 60_000)
    public void logLeagueSummary() {
        log.info("[scheduled fixedRate] League snapshot — teams={}, players={}, matches={}",
                teamRepo.count(), playerRepo.count(), matchRepo.count());
    }

    // Fires at the top of every hour. Useful example of full cron syntax (sec min hr day mon dow).
    @Scheduled(cron = "0 0 * * * *")
    public void hourlyCleanupCheck() {
        log.info("[scheduled cron] Hourly cleanup check — nothing to reap in this demo.");
    }
}
