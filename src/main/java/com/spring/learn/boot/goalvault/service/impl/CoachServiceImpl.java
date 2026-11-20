package com.spring.learn.boot.goalvault.service.impl;

import com.spring.learn.boot.goalvault.config.AppInfoConfig;
import com.spring.learn.boot.goalvault.dto.CoachRequest;
import com.spring.learn.boot.goalvault.dto.CoachResponse;
import com.spring.learn.boot.goalvault.entity.Coach;
import com.spring.learn.boot.goalvault.entity.Team;
import com.spring.learn.boot.goalvault.exception.ResourceNotFoundException;
import com.spring.learn.boot.goalvault.repository.CoachRepository;
import com.spring.learn.boot.goalvault.repository.TeamRepository;
import com.spring.learn.boot.goalvault.service.CoachService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CoachServiceImpl implements CoachService {

    private static final Logger log = LoggerFactory.getLogger(CoachServiceImpl.class);

    private final CoachRepository coachRepo;
    private final TeamRepository teamRepo;

    // -----------------------------------------------------------------------------------------
    // LEARNING — DEPENDENCY INJECTION #3: SETTER INJECTION (used for OPTIONAL dependencies).
    // -----------------------------------------------------------------------------------------
    // AppInfo is a diagnostic-only bean; the service works without it. Setter injection makes
    // sense when:
    //   * The dependency is genuinely optional (required=false lets Spring skip if bean is missing).
    //   * The dependency may need to change AFTER construction (rare in Spring apps but possible
    //     in test/reconfiguration scenarios).
    //   * You want to keep the constructor slim and reserved for required collaborators.
    // Downside vs constructor injection: the bean has a valid-but-partially-configured state
    // between construction and setter invocation, and the field can't be `final`.
    // -----------------------------------------------------------------------------------------
    private AppInfoConfig.AppInfo appInfo;

    @Autowired(required = false)
    public void setAppInfo(AppInfoConfig.AppInfo appInfo) {
        this.appInfo = appInfo;
        log.debug("Setter injection wired AppInfo into CoachServiceImpl: {}", appInfo);
    }

    public CoachServiceImpl(CoachRepository coachRepo, TeamRepository teamRepo) {
        this.coachRepo = coachRepo;
        this.teamRepo = teamRepo;
    }

    @Override
    @Transactional
    public CoachResponse create(CoachRequest req) {
        Coach c = new Coach();
        apply(c, req);
        return toDto(coachRepo.save(c));
    }

    @Override
    @Transactional
    public CoachResponse update(Long id, CoachRequest req) {
        Coach c = coachRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Coach", id));
        apply(c, req);
        return toDto(coachRepo.save(c));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!coachRepo.existsById(id)) throw new ResourceNotFoundException("Coach", id);
        coachRepo.deleteById(id);
    }

    @Override
    public CoachResponse get(Long id) {
        return coachRepo.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Coach", id));
    }

    @Override
    public Page<CoachResponse> list(Pageable pageable) {
        return coachRepo.findAll(pageable).map(this::toDto);
    }

    private void apply(Coach c, CoachRequest r) {
        Team team = teamRepo.findById(r.teamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team", r.teamId()));
        c.setName(r.name());
        c.setTeam(team);
        c.setNationality(r.nationality());
    }

    private CoachResponse toDto(Coach c) {
        Team t = c.getTeam();
        return new CoachResponse(
                c.getId(), c.getName(), c.getNationality(),
                t == null ? null : t.getId(),
                t == null ? null : t.getName());
    }
}
