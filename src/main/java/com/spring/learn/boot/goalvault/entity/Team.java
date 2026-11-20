package com.spring.learn.boot.goalvault.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// LEARNING: @Entity marks this POJO as a JPA-managed persistent class; Hibernate maps it to a DB table.
@Entity
// LEARNING: @Table overrides the auto-derived table name (defaults to the class name).
@Table(name = "teams")
// LEARNING: Lombok @Getter/@Setter generate accessors at compile time so we skip boilerplate.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Team {

    // LEARNING: @Id marks the primary-key field.
    @Id
    // LEARNING: @GeneratedValue(IDENTITY) delegates PK generation to the DB (auto-increment / identity column).
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // LEARNING: @Column customises column-level mapping (nullability, length, name, etc.).
    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 80)
    private String city;

    @Column(name = "founded_year")
    private Integer foundedYear;

    @Column(name = "stadium_name", length = 120)
    private String stadiumName;

    // LEARNING: @OneToMany is the inverse side; mappedBy points to the field in Player that owns the FK.
    // cascade=ALL propagates persist/remove ops to child players; orphanRemoval deletes children
    // dropped from this list. fetch=LAZY avoids loading players until explicitly accessed.
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Player> players = new ArrayList<>();

    // Inverse side of the Coach OneToOne — Coach owns the FK.
    @OneToOne(mappedBy = "team", fetch = FetchType.LAZY)
    private Coach coach;
}
