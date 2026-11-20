package com.spring.learn.boot.goalvault.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// LEARNING: A CUSTOM Bean Validation constraint. It marries an annotation with a ConstraintValidator
// implementation. Runtime scan: any field/parameter/method annotated with @ValidPosition is validated
// by PositionValidator whenever a @Valid object graph is checked.
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
// LEARNING: @Constraint wires this annotation to the validator class. The validation engine finds
// PositionValidator via this metadata — nothing needs to be registered manually.
@Constraint(validatedBy = PositionValidator.class)
public @interface ValidPosition {

    String message() default "position must be one of: GOALKEEPER, DEFENDER, MIDFIELDER, FORWARD (case-insensitive)";

    // Required boilerplate for JSR-380. Empty defaults are fine.
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    // If true, null is treated as a valid value (skip check). Combine with @NotNull when you want to reject nulls.
    boolean allowNull() default false;
}
