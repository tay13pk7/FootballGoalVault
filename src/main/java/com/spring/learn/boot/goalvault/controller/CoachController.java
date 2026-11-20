package com.spring.learn.boot.goalvault.controller;

import com.spring.learn.boot.goalvault.dto.CoachRequest;
import com.spring.learn.boot.goalvault.dto.CoachResponse;
import com.spring.learn.boot.goalvault.service.CoachService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coaches")
public class CoachController {

    private final CoachService service;

    public CoachController(CoachService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CoachResponse> create(@Valid @RequestBody CoachRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping("/{id}")
    public CoachResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public Page<CoachResponse> list(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return service.list(pageable);
    }

    @PutMapping("/{id}")
    public CoachResponse update(@PathVariable Long id, @Valid @RequestBody CoachRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
