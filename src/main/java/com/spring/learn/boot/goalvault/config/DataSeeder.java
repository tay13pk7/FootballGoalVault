package com.spring.learn.boot.goalvault.config;

import com.spring.learn.boot.goalvault.entity.Coach;
import com.spring.learn.boot.goalvault.entity.Fan;
import com.spring.learn.boot.goalvault.entity.Match;
import com.spring.learn.boot.goalvault.entity.MatchReview;
import com.spring.learn.boot.goalvault.entity.Player;
import com.spring.learn.boot.goalvault.entity.Team;
import com.spring.learn.boot.goalvault.repository.CoachRepository;
import com.spring.learn.boot.goalvault.repository.FanRepository;
import com.spring.learn.boot.goalvault.repository.MatchRepository;
import com.spring.learn.boot.goalvault.repository.MatchReviewRepository;
import com.spring.learn.boot.goalvault.repository.PlayerRepository;
import com.spring.learn.boot.goalvault.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

// LEARNING: @Component is the generic Spring stereotype; @Service/@Repository/@Controller all extend it.
// A CommandLineRunner bean's run(...) method executes once after the ApplicationContext is ready.
// LEARNING: @Profile("dev") makes this bean visible ONLY when the "dev" profile is active
// (see spring.profiles.default=dev in application.yml). In prod, ProdDataSeeder runs instead —
// so we don't shove 300 review rows into a real database on startup.
@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final TeamRepository teamRepo;
    private final PlayerRepository playerRepo;
    private final MatchRepository matchRepo;
    private final CoachRepository coachRepo;
    private final FanRepository fanRepo;
    private final MatchReviewRepository reviewRepo;

    public DataSeeder(TeamRepository teamRepo, PlayerRepository playerRepo, MatchRepository matchRepo,
                      CoachRepository coachRepo, FanRepository fanRepo, MatchReviewRepository reviewRepo) {
        this.teamRepo = teamRepo;
        this.playerRepo = playerRepo;
        this.matchRepo = matchRepo;
        this.coachRepo = coachRepo;
        this.fanRepo = fanRepo;
        this.reviewRepo = reviewRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (teamRepo.count() > 0) {
            log.info("GoalVault seed skipped — data already present.");
            return;
        }
        Random rnd = new Random(42);

        String[] teamNames = {
                "Northgate United", "Riverside FC", "Highland Rovers", "Coastal Kings", "Ironbridge Athletic",
                "Silver Falcons", "Meadowbrook City", "Sunset Strikers", "Oakwood Wanderers", "Crimson Lions",
                "Emerald Eagles", "Golden Harbor", "Frost Peak FC", "Amber Thunder", "Velocity SC"
        };
        String[] cities = {"London", "Barcelona", "Munich", "Paris", "Milan", "Madrid", "Amsterdam",
                "Lisbon", "Manchester", "Naples", "Porto", "Turin", "Seville", "Lyon", "Rotterdam"};
        String[] stadiums = {"North Park Arena", "Riverside Stadium", "Highland Ground", "Coastal Dome",
                "Iron Field", "Falcon Nest", "Meadow Arena", "Sunset Bowl", "Oak Stadium", "Lion's Den"};
        String[] positions = {"Goalkeeper", "Defender", "Midfielder", "Forward"};
        String[] nationalities = {"Brazilian", "Argentinian", "German", "Spanish", "French", "Italian",
                "English", "Dutch", "Portuguese", "Belgian"};

        // Teams
        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < teamNames.length; i++) {
            Team t = new Team();
            t.setName(teamNames[i]);
            t.setCity(cities[i % cities.length]);
            t.setFoundedYear(1880 + rnd.nextInt(140));
            t.setStadiumName(stadiums[i % stadiums.length]);
            teams.add(t);
        }
        teams = teamRepo.saveAll(teams);

        // Coaches — one per team (~15)
        List<Coach> coaches = new ArrayList<>();
        for (int i = 0; i < teams.size(); i++) {
            Coach c = new Coach();
            c.setName("Coach " + (i + 1) + " " + randomSurname(rnd));
            c.setTeam(teams.get(i));
            c.setNationality(nationalities[rnd.nextInt(nationalities.length)]);
            coaches.add(c);
        }
        coachRepo.saveAll(coaches);

        // Players — 40 spread across teams
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            Player p = new Player();
            p.setName(randomFirstName(rnd) + " " + randomSurname(rnd));
            p.setPosition(positions[rnd.nextInt(positions.length)]);
            p.setJerseyNumber(1 + rnd.nextInt(99));
            p.setTeam(teams.get(rnd.nextInt(teams.size())));
            p.setGoalsScored(rnd.nextInt(30));
            players.add(p);
        }
        players = playerRepo.saveAll(players);

        // Matches — 150
        List<Match> matches = new ArrayList<>();
        LocalDateTime seasonStart = LocalDateTime.of(2024, 8, 1, 15, 0);
        for (int i = 0; i < 150; i++) {
            Team home = teams.get(rnd.nextInt(teams.size()));
            Team away;
            do {
                away = teams.get(rnd.nextInt(teams.size()));
            } while (away.getId().equals(home.getId()));

            Match m = new Match();
            m.setHomeTeam(home);
            m.setAwayTeam(away);
            m.setMatchDate(seasonStart.plusDays(i).plusHours(rnd.nextInt(6)));
            m.setHomeScore(rnd.nextInt(5));
            m.setAwayScore(rnd.nextInt(5));
            m.setStadium(home.getStadiumName());
            matches.add(m);
        }
        matches = matchRepo.saveAll(matches);

        // Fans — a few, each with a favorite team and some followed players
        String[] fanNames = {"alice", "bob", "carol", "dave", "eve", "frank", "grace", "heidi"};
        List<Fan> fans = new ArrayList<>();
        for (int i = 0; i < fanNames.length; i++) {
            Fan f = new Fan();
            f.setUsername(fanNames[i]);
            f.setPassword("{noop}secret" + i);
            f.setFavoriteTeam(teams.get(rnd.nextInt(teams.size())));
            Set<Player> followed = new HashSet<>();
            for (int j = 0; j < 5; j++) followed.add(players.get(rnd.nextInt(players.size())));
            f.setFollowedPlayers(followed);
            fans.add(f);
        }
        fans = fanRepo.saveAll(fans);

        // Match reviews — 300
        String[] blurbs = {
                "What a match!", "Boring first half.", "Solid defensive display.", "Awful refereeing.",
                "Best game of the season.", "Ended in a fair draw.", "Instant classic.", "Forgettable ninety minutes.",
                "Underdog upset — loved it.", "Would rewatch."
        };
        List<MatchReview> reviews = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            MatchReview r = new MatchReview();
            r.setMatch(matches.get(rnd.nextInt(matches.size())));
            r.setFan(fans.get(rnd.nextInt(fans.size())));
            r.setRating(1 + rnd.nextInt(5));
            r.setComment(blurbs[rnd.nextInt(blurbs.length)]);
            reviews.add(r);
        }
        reviewRepo.saveAll(reviews);

        log.info("GoalVault seed complete: {} teams, {} coaches, {} players, {} matches, {} fans, {} reviews",
                teams.size(), coaches.size(), players.size(), matches.size(), fans.size(), reviews.size());
    }

    private static final String[] FIRST_NAMES = {"Leo", "Marco", "Diego", "Tomas", "Kai", "Luka", "Enzo", "Rafael", "Julian", "Nico"};
    private static final String[] SURNAMES    = {"Silva", "Rossi", "Muller", "Garcia", "Duarte", "Van Dijk", "Ferrari", "Costa", "Novak", "Ibrahim"};

    private String randomFirstName(Random r) { return FIRST_NAMES[r.nextInt(FIRST_NAMES.length)]; }
    private String randomSurname(Random r)   { return SURNAMES[r.nextInt(SURNAMES.length)]; }
}
