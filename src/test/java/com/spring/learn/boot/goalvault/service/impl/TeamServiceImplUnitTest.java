package com.spring.learn.boot.goalvault.service.impl;

import com.spring.learn.boot.goalvault.dto.TeamRequest;
import com.spring.learn.boot.goalvault.dto.TeamResponse;
import com.spring.learn.boot.goalvault.entity.Team;
import com.spring.learn.boot.goalvault.exception.ResourceNotFoundException;
import com.spring.learn.boot.goalvault.repository.MatchReviewRepository;
import com.spring.learn.boot.goalvault.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * PURE UNIT TEST — no Spring context, just JUnit + Mockito. Chosen over any Spring-based test type
 * because we only want to exercise TeamServiceImpl's own logic (mapping, error paths). Repositories
 * are mocked; there is nothing to autowire, no proxies to boot. Fastest possible feedback loop.
 */
@ExtendWith(MockitoExtension.class)
class TeamServiceImplUnitTest {

    @Mock private TeamRepository repo;
    @Mock private MatchReviewRepository reviewRepo;

    @InjectMocks private TeamServiceImpl service;

    @Test
    void createPersistsAndReturnsDto() {
        Team saved = new Team();
        saved.setId(42L);
        saved.setName("Unit FC");
        saved.setCity("Testville");
        saved.setFoundedYear(2000);
        saved.setStadiumName("Unit Ground");
        when(repo.save(any(Team.class))).thenReturn(saved);

        TeamResponse resp = service.create(new TeamRequest("Unit FC", "Testville", 2000, "Unit Ground"));

        assertThat(resp.id()).isEqualTo(42L);
        assertThat(resp.name()).isEqualTo("Unit FC");
    }

    @Test
    void getUnknownIdRaisesNotFound() {
        when(repo.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
