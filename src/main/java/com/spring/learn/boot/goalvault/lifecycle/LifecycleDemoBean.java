package com.spring.learn.boot.goalvault.lifecycle;

import com.spring.learn.boot.goalvault.repository.MatchRepository;
import com.spring.learn.boot.goalvault.repository.PlayerRepository;
import com.spring.learn.boot.goalvault.repository.TeamRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Demonstrates the classic Spring bean-lifecycle hooks.
 *
 * LEARNING:
 *   @PostConstruct — invoked ONCE, after Spring has finished injecting dependencies, before the
 *                    bean is put into service. Use for "prime the pump" work (warm caches, log
 *                    startup diagnostics, validate config). Ideally cheap; ApplicationRunner /
 *                    CommandLineRunner is a better fit for long-running startup work.
 *   @PreDestroy   — invoked when the ApplicationContext is closing. Use to release resources
 *                    the JVM can't reclaim on its own (connections, temp files, native handles).
 *                    Only fires on graceful shutdown; kill -9 skips it.
 */
@Component
public class LifecycleDemoBean {

    private static final Logger log = LoggerFactory.getLogger(LifecycleDemoBean.class);

    private final TeamRepository teamRepo;
    private final PlayerRepository playerRepo;
    private final MatchRepository matchRepo;

    public LifecycleDemoBean(TeamRepository teamRepo, PlayerRepository playerRepo, MatchRepository matchRepo) {
        this.teamRepo = teamRepo;
        this.playerRepo = playerRepo;
        this.matchRepo = matchRepo;
    }

    @PostConstruct
    public void onStartup() {
        log.info("[lifecycle @PostConstruct] GoalVault is up — teams={}, players={}, matches={}",
                teamRepo.count(), playerRepo.count(), matchRepo.count());
    }

    @PreDestroy
    public void onShutdown() {
        log.info("[lifecycle @PreDestroy] GoalVault is shutting down — goodbye.");
    }
}
