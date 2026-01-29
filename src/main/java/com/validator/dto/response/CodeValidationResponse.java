package com.validator.dto.response;

import com.validator.domain.model.AnalysisSummary;
import com.validator.domain.model.DecisionResult;

import java.util.List;

public record CodeValidationResponse(
        DecisionResult decision,
        AnalysisSummary analysis,
        List<String> suggestions
) {
}
