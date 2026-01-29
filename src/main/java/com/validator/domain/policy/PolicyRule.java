package com.validator.domain.policy;

import com.validator.domain.model.IssueSeverity;
import com.validator.domain.model.IssueType;

public record PolicyRule(
        String id,
        String name,
        IssueType type,
        IssueSeverity minSeverity,
        int minCount,
        PolicyAction action
) {
}
