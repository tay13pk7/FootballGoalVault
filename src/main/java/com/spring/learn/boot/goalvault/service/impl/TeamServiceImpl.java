package com.spring.learn.boot.goalvault.service.impl;

import com.spring.learn.boot.goalvault.dto.TeamRequest;
import com.spring.learn.boot.goalvault.dto.TeamResponse;
import com.spring.learn.boot.goalvault.dto.TeamResponseV2;
import com.spring.learn.boot.goalvault.entity.Team;
import com.spring.learn.boot.goalvault.exception.ResourceNotFoundException;
import com.spring.learn.boot.goalvault.repository.MatchReviewRepository;
import com.spring.learn.boot.goalvault.repository.TeamRepository;
import com.spring.learn.boot.goalvault.service.TeamService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// LEARNING: @Service is a specialization of @Component signalling "business logic layer".
// Functionally identical to @Component but improves intent and enables layered classpath scanning.
@Service
// LEARNING: @Transactional at class level wraps every public method in a DB transaction.
// readOnly=true is an optimization hint — Hibernate skips dirty checks for read-only operations.
// Individual methods can override this (see mutating methods below).
@Transactional(readOnly = true)
public class TeamServiceImpl implements TeamService {

    private final TeamRepository repo;
    private final MatchReviewRepository reviewRepo;

    // -----------------------------------------------------------------------------------------
    // LEARNING — DEPENDENCY INJECTION #1: CONSTRUCTOR INJECTION (recommended default).
    // -----------------------------------------------------------------------------------------
    // Why constructor injection is the sane default:
    //   1) IMMUTABILITY — dependencies are `final`; the object is fully wired the moment it exists.
    //   2) TESTABILITY — you can `new TeamServiceImpl(mockRepo, mockReviewRepo)` without any
    //      Spring context. Unit tests stay fast and honest.
    //   3) NO CIRCULAR-DEPENDENCY MASKING — constructor cycles fail LOUDLY at startup instead of
    //      silently working via field injection (which lets Spring wire a half-baked bean).
    //   4) EXPLICIT REQUIRED DEPENDENCIES — the constructor signature is the class's contract.
    //      A reader immediately knows what this class needs to function.
    // Since Spring 4.3, a single constructor auto-wires — @Autowired is optional and omitted.
    // -----------------------------------------------------------------------------------------
    public TeamServiceImpl(TeamRepository repo, MatchReviewRepository reviewRepo) {
        this.repo = repo;
        this.reviewRepo = reviewRepo;
    }

    @Override
    @Transactional
    public TeamResponse create(TeamRequest req) {
        Team t = new Team();
        apply(t, req);
        return toDto(repo.save(t));
    }

    @Override
    @Transactional
    // LEARNING: @CacheEvict removes the entry so the NEXT read re-populates from the DB.
    // Choose @CacheEvict for updates when the new value is easy to fetch afresh, or when the
    // update method doesn't naturally return the whole cached shape.
    @CacheEvict(cacheNames = "teams", key = "#id")
    public TeamResponse update(Long id, TeamRequest req) {
        Team t = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Team", id));
        apply(t, req);
        return toDto(repo.save(t));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "teams", key = "#id")
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("Team", id);
        repo.deleteById(id);
    }

    @Override
    // LEARNING: @Cacheable memoizes by key. On a miss, run the method + cache the result; on a
    // hit, skip the method entirely. Backed by the default ConcurrentMapCacheManager here.
    @Cacheable(cacheNames = "teams", key = "#id")
    public TeamResponse get(Long id) {
        return repo.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));
    }

    @Override
    public TeamResponseV2 getV2(Long id) {
        Team t = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Team", id));
        Double avg = reviewRepo.averageRatingForTeam(id);
        int players = t.getPlayers() == null ? 0 : t.getPlayers().size();
        return new TeamResponseV2(t.getId(), t.getName(), t.getCity(), t.getFoundedYear(),
                t.getStadiumName(), players, avg);
    }

    @Override
    public Page<TeamResponse> list(String city, Pageable pageable) {
        Page<Team> page = (city == null || city.isBlank())
                ? repo.findAll(pageable)
                : repo.findByCityIgnoreCase(city, pageable);
        return page.map(this::toDto);
    }

    @Override
    @Transactional
    // LEARNING: @CachePut ALWAYS runs the method, then puts the return value into the cache.
    // Use @CachePut when the method already computes the exact object you want in the cache —
    // saves the follow-up "get" round-trip that @CacheEvict + next-read forces.
    //
    // Rule of thumb:
    //   @CachePut  — mutation returns the fresh cached shape; keep the cache warm.
    //   @CacheEvict — mutation's return value doesn't match the cache shape, or you'd rather
    //                 refetch on the next read (guarantees consistency with any DB triggers).
    @CachePut(cacheNames = "teams", key = "#id")
    public TeamResponse updateStadium(Long id, String stadiumName) {
        Team t = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Team", id));
        t.setStadiumName(stadiumName);
        return toDto(repo.save(t));
    }

    private void apply(Team t, TeamRequest r) {
        t.setName(r.name());
        t.setCity(r.city());
        t.setFoundedYear(r.foundedYear());
        t.setStadiumName(r.stadiumName());
    }

    private TeamResponse toDto(Team t) {
        return new TeamResponse(t.getId(), t.getName(), t.getCity(), t.getFoundedYear(), t.getStadiumName(),
                t.getPlayers() == null ? 0 : t.getPlayers().size());
    }
}
