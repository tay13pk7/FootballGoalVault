package com.spring.learn.boot.goalvault.service.impl;

import com.spring.learn.boot.goalvault.dto.MatchRequest;
import com.spring.learn.boot.goalvault.dto.MatchResponse;
import com.spring.learn.boot.goalvault.entity.Match;
import com.spring.learn.boot.goalvault.entity.Team;
import com.spring.learn.boot.goalvault.exception.ResourceNotFoundException;
import com.spring.learn.boot.goalvault.repository.MatchRepository;
import com.spring.learn.boot.goalvault.repository.TeamRepository;
import com.spring.learn.boot.goalvault.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepo;

    // -----------------------------------------------------------------------------------------
    // LEARNING — DEPENDENCY INJECTION #2: FIELD INJECTION (deliberately atypical, avoid in real code).
    // -----------------------------------------------------------------------------------------
    // Downsides of field injection:
    //   1) HARDER TO TEST — you can't `new MatchServiceImpl(mockRepo)` and pre-set teamRepo without
    //      reflection or a Spring context. Every unit test has to work around the missing setter.
    //   2) HIDES REQUIRED DEPENDENCIES — the class contract is scattered across @Autowired fields
    //      instead of one clear constructor signature.
    //   3) ALLOWS CIRCULAR DEPENDENCIES TO COMPILE — Spring can wire cycles via field injection
    //      because it constructs the bean first, then patches fields. Constructor injection would
    //      have failed loudly; field injection lets the bug slip into production.
    //   4) CANNOT BE `final` — the field must remain mutable so Spring can set it, defeating
    //      the immutability that makes concurrent code safe by construction.
    // Kept here purely to demonstrate the annotation. Prefer constructor injection everywhere else.
    // -----------------------------------------------------------------------------------------
    @Autowired
    private TeamRepository teamRepo;

    public MatchServiceImpl(MatchRepository matchRepo) {
        this.matchRepo = matchRepo;
    }

    @Override
    @Transactional
    public MatchResponse create(MatchRequest req) {
        Match m = new Match();
        apply(m, req);
        return toDto(matchRepo.save(m));
    }

    @Override
    @Transactional
    public MatchResponse update(Long id, MatchRequest req) {
        Match m = matchRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Match", id));
        apply(m, req);
        return toDto(matchRepo.save(m));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!matchRepo.existsById(id)) throw new ResourceNotFoundException("Match", id);
        matchRepo.deleteById(id);
    }

    @Override
    public MatchResponse get(Long id) {
        return matchRepo.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Match", id));
    }

    @Override
    public Page<MatchResponse> list(Long teamId, String stadium, Pageable pageable) {
        Page<Match> page;
        if (teamId != null)                          page = matchRepo.findByHomeTeamIdOrAwayTeamId(teamId, teamId, pageable);
        else if (stadium != null && !stadium.isBlank()) page = matchRepo.findByStadiumIgnoreCase(stadium, pageable);
        else                                          page = matchRepo.findAll(pageable);
        return page.map(this::toDto);
    }

    private void apply(Match m, MatchRequest r) {
        if (r.homeTeamId().equals(r.awayTeamId())) {
            throw new IllegalArgumentException("homeTeamId and awayTeamId must be different");
        }
        Team home = teamRepo.findById(r.homeTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team", r.homeTeamId()));
        Team away = teamRepo.findById(r.awayTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team", r.awayTeamId()));
        m.setHomeTeam(home);
        m.setAwayTeam(away);
        m.setMatchDate(r.matchDate());
        m.setHomeScore(r.homeScore());
        m.setAwayScore(r.awayScore());
        m.setStadium(r.stadium());
    }

    private MatchResponse toDto(Match m) {
        Team home = m.getHomeTeam();
        Team away = m.getAwayTeam();
        return new MatchResponse(
                m.getId(),
                home == null ? null : home.getId(),
                home == null ? null : home.getName(),
                away == null ? null : away.getId(),
                away == null ? null : away.getName(),
                m.getMatchDate(), m.getHomeScore(), m.getAwayScore(), m.getStadium());
    }
}
