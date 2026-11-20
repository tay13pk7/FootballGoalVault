package com.spring.learn.boot.goalvault.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 40)
    private String position;

    @Column(name = "jersey_number")
    private Integer jerseyNumber;

    // LEARNING: @ManyToOne is the owning side of the relationship — the FK column lives on THIS table.
    // fetch=LAZY prevents eager loading; @JoinColumn names the FK column.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "goals_scored", nullable = false)
    private Integer goalsScored = 0;

    // Inverse side of the Fan.followedPlayers ManyToMany — Fan owns the join table.
    @ManyToMany(mappedBy = "followedPlayers", fetch = FetchType.LAZY)
    private Set<Fan> followers = new HashSet<>();
}
