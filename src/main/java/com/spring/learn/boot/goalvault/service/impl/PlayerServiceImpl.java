package com.spring.learn.boot.goalvault.service.impl;

import com.spring.learn.boot.goalvault.dto.PlayerRequest;
import com.spring.learn.boot.goalvault.dto.PlayerResponse;
import com.spring.learn.boot.goalvault.entity.Player;
import com.spring.learn.boot.goalvault.entity.Team;
import com.spring.learn.boot.goalvault.exception.ResourceNotFoundException;
import com.spring.learn.boot.goalvault.repository.PlayerRepository;
import com.spring.learn.boot.goalvault.repository.PlayerSpecifications;
import com.spring.learn.boot.goalvault.repository.TeamRepository;
import com.spring.learn.boot.goalvault.service.PlayerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepo;
    private final TeamRepository teamRepo;

    public PlayerServiceImpl(PlayerRepository playerRepo, TeamRepository teamRepo) {
        this.playerRepo = playerRepo;
        this.teamRepo = teamRepo;
    }

    @Override
    @Transactional
    public PlayerResponse create(PlayerRequest req) {
        Player p = new Player();
        apply(p, req);
        return toDto(playerRepo.save(p));
    }

    @Override
    @Transactional
    public PlayerResponse update(Long id, PlayerRequest req) {
        Player p = playerRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Player", id));
        apply(p, req);
        return toDto(playerRepo.save(p));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!playerRepo.existsById(id)) throw new ResourceNotFoundException("Player", id);
        playerRepo.deleteById(id);
    }

    @Override
    public PlayerResponse get(Long id) {
        return playerRepo.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Player", id));
    }

    @Override
    public Page<PlayerResponse> list(Long teamId, String position, Integer minGoals, String q, Pageable pageable) {
        // LEARNING: Dynamic filter composition via Specifications. Only non-null params contribute.
        Specification<Player> spec = PlayerSpecifications.alwaysTrue();
        if (teamId != null)                     spec = spec.and(PlayerSpecifications.hasTeam(teamId));
        if (position != null && !position.isBlank())
                                                spec = spec.and(PlayerSpecifications.hasPositionIgnoreCase(position));
        if (minGoals != null)                   spec = spec.and(PlayerSpecifications.goalsAtLeast(minGoals));
        if (q != null && !q.isBlank())          spec = spec.and(PlayerSpecifications.nameContainsIgnoreCase(q));
        return playerRepo.findAll(spec, pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public PlayerResponse scoreGoal(Long id) {
        Player p = playerRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Player", id));
        p.setGoalsScored(p.getGoalsScored() + 1);
        return toDto(playerRepo.save(p));
    }

    private void apply(Player p, PlayerRequest r) {
        Team team = teamRepo.findById(r.teamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team", r.teamId()));
        p.setName(r.name());
        // Normalize position to uppercase to match the @ValidPosition allowed set.
        p.setPosition(r.position() == null ? null : r.position().trim().toUpperCase());
        p.setJerseyNumber(r.jerseyNumber());
        p.setTeam(team);
        p.setGoalsScored(r.goalsScored());
    }

    private PlayerResponse toDto(Player p) {
        Team t = p.getTeam();
        return new PlayerResponse(
                p.getId(), p.getName(), p.getPosition(), p.getJerseyNumber(), p.getGoalsScored(),
                t == null ? null : t.getId(),
                t == null ? null : t.getName());
    }
}
