package com.validator.domain.model;

import java.util.List;

public record DecisionResult(
        DecisionStatus status,
        String reason,
        List<String> triggeredRules
) {
}
