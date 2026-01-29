package com.validator.domain.model;

public record CodeSubmission(
        String sourceCode,
        String filePath,
        String context,
        String language
) {
}
