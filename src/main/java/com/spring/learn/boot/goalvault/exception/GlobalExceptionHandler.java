package com.spring.learn.boot.goalvault.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

// LEARNING: @RestControllerAdvice = @ControllerAdvice + @ResponseBody. It intercepts exceptions
// thrown from ANY @RestController and lets us return uniform JSON error payloads.
//
// This handler emits RFC 7807 "Problem Details for HTTP APIs" bodies using Spring's built-in
// ProblemDetail (added in Spring Framework 6). Every response carries: type, title, status,
// detail, instance, timestamp, and optionally fieldErrors as an extension member.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // LEARNING: @ExceptionHandler binds a method to a specific exception type. Spring picks the
    // most-specific handler in the advice for the thrown exception.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        ProblemDetail pd = base(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), req);
        pd.setType(URI.create("https://goalvault.dev/problems/not-found"));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(ConflictException ex, HttpServletRequest req) {
        ProblemDetail pd = base(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req);
        pd.setType(URI.create("https://goalvault.dev/problems/conflict"));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }

    // Fires when @Valid on a @RequestBody fails.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        ProblemDetail pd = base(HttpStatus.BAD_REQUEST, "Validation Failed",
                "One or more fields have invalid values.", req);
        pd.setType(URI.create("https://goalvault.dev/problems/validation"));
        // LEARNING: setProperty adds RFC 7807 "extension members" (any additional JSON fields).
        pd.setProperty("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArg(IllegalArgumentException ex, HttpServletRequest req) {
        ProblemDetail pd = base(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req);
        pd.setType(URI.create("https://goalvault.dev/problems/bad-request"));
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleAll(Exception ex, HttpServletRequest req) {
        // Never leak stack traces to clients. Log the exception; return a generic 500 problem body.
        log.error("Unhandled exception on {}", req.getRequestURI(), ex);
        ProblemDetail pd = base(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred.", req);
        pd.setType(URI.create("https://goalvault.dev/problems/internal"));
        return ResponseEntity.internalServerError().body(pd);
    }

    private ProblemDetail base(HttpStatus status, String title, String detail, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }
}
