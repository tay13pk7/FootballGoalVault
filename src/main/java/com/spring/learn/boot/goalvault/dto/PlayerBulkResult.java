package com.spring.learn.boot.goalvault.dto;

// LEARNING: Bulk endpoint result item — one entry per input request in the batch, so partial success
// can be reported (unlike returning HTTP 400 for the entire batch when one item fails validation).
public record PlayerBulkResult(
        int index,
        boolean success,
        Long id,           // populated on success
        String error       // populated on failure
) {
    public static PlayerBulkResult ok(int index, Long id) {
        return new PlayerBulkResult(index, true, id, null);
    }
    public static PlayerBulkResult failed(int index, String error) {
        return new PlayerBulkResult(index, false, null, error);
    }
}
