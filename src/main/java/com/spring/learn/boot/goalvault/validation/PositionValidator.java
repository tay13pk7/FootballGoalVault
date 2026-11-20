package com.spring.learn.boot.goalvault.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

// LEARNING: A ConstraintValidator implements the actual check for a custom annotation.
// isValid returns true when the value passes validation; false otherwise.
public class PositionValidator implements ConstraintValidator<ValidPosition, String> {

    // Allowed positions. Kept as a String set (not enum) because Player.position is a String field
    // in the current data model — switching to an enum would be a schema change.
    private static final Set<String> ALLOWED = Set.of("GOALKEEPER", "DEFENDER", "MIDFIELDER", "FORWARD");

    private boolean allowNull;

    @Override
    public void initialize(ValidPosition annotation) {
        this.allowNull = annotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return allowNull;
        return ALLOWED.contains(value.trim().toUpperCase());
    }
}
