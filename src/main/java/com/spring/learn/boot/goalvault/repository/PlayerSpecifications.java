package com.spring.learn.boot.goalvault.repository;

import com.spring.learn.boot.goalvault.entity.Player;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

// LEARNING: JPA Specifications compose Criteria API predicates into reusable pieces.
// Combined with JpaSpecificationExecutor, they enable DYNAMIC filtering — only the predicates
// that correspond to non-null query params get ANDed together at runtime.
public final class PlayerSpecifications {

    private PlayerSpecifications() { }

    public static Specification<Player> hasTeam(Long teamId) {
        return (root, query, cb) -> cb.equal(root.get("team").get("id"), teamId);
    }

    public static Specification<Player> hasPositionIgnoreCase(String position) {
        return (root, query, cb) -> cb.equal(cb.upper(root.get("position")), position.trim().toUpperCase());
    }

    public static Specification<Player> goalsAtLeast(int minGoals) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("goalsScored"), minGoals);
    }

    public static Specification<Player> nameContainsIgnoreCase(String q) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + q.trim().toLowerCase() + "%");
    }

    // Helper: an always-true predicate so we can seed a Specification and .and(...) more onto it
    // without null-checks at every call-site.
    public static Specification<Player> alwaysTrue() {
        return (root, query, cb) -> cb.conjunction();
    }
}
