package com.validator.application;

import com.validator.domain.policy.PolicyEngine;
import com.validator.dto.request.CodeValidationRequest;
import com.validator.exception.ValidationException;
import com.validator.infrastructure.ai.AiAnalysisService;
import com.validator.infrastructure.analyzer.SecurityValidator;
import com.validator.infrastructure.analyzer.StaticAnalyzer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CodeValidationServiceTest {

    @Test
    void throwsValidationExceptionWhenForbiddenPatternDetected() {
        AiAnalysisService aiAnalysisService = mock(AiAnalysisService.class);
        when(aiAnalysisService.analyze(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        List<StaticAnalyzer> analyzers = List.of(new SecurityValidator());
        PolicyEngine policyEngine = new PolicyEngine();

        CodeValidationService service = new CodeValidationService(analyzers, aiAnalysisService, policyEngine);

        CodeValidationRequest request = new CodeValidationRequest(
                "public class A { void run(){ Runtime.getRuntime().exec(\"rm\"); } }",
                "A.java",
                "test",
                "JAVA"
        );

        assertThatThrownBy(() -> service.review(request))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> assertThat(((ValidationException) ex).getCode())
                        .isEqualTo("FORBIDDEN_PATTERN"));
    }
}
