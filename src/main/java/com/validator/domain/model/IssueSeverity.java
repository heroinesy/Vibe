package com.validator.domain.model;

public enum IssueSeverity {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int level;

    IssueSeverity(int level) {
        this.level = level;
    }

    public boolean isAtLeast(IssueSeverity other) {
        return this.level >= other.level;
    }
}
