package com.validator.domain.model;

public record Issue(
        IssueType type,
        IssueSeverity severity,
        String message,
        Integer line,
        IssueSource source
) {
}
