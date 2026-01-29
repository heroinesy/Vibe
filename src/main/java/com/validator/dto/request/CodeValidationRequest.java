package com.validator.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CodeValidationRequest(
        @NotBlank String sourceCode,
        String filePath,
        String context,
        String language
) {
}
