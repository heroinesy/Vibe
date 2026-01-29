package com.validator.domain.model;

import java.util.List;

public record AnalysisSummary(
        List<Issue> issues,
        int riskScore,
        boolean hasBlockingIssues
) {
}
