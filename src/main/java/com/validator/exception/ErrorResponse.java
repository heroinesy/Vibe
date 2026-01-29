package com.validator.exception;

public record ErrorResponse(
        String code,
        String message
) {
}
