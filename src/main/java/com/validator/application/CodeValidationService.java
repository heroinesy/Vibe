package com.validator.application;

import com.validator.domain.model.AnalysisSummary;
import com.validator.domain.model.CodeSubmission;
import com.validator.domain.model.DecisionResult;
import com.validator.domain.model.Issue;
import com.validator.domain.model.IssueSeverity;
import com.validator.domain.model.IssueType;
import com.validator.domain.model.DecisionStatus;
import com.validator.dto.request.CodeValidationRequest;
import com.validator.dto.response.CodeValidationResponse;
import com.validator.domain.policy.PolicyEngine;
import com.validator.exception.ValidationException;
import com.validator.infrastructure.ai.AiAnalysisService;
import com.validator.infrastructure.analyzer.StaticAnalyzer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CodeValidationService {

    private static final int MAX_SOURCE_LENGTH = 20000;
    private static final List<String> FORBIDDEN_PATTERNS = List.of(
            "Runtime.getRuntime().exec",
            "ProcessBuilder",
            "System.exit"
    );

    private final List<StaticAnalyzer> staticAnalyzers;
    private final AiAnalysisService aiAnalysisService;
    private final PolicyEngine policyEngine;

    public CodeValidationService(
            List<StaticAnalyzer> staticAnalyzers,
            AiAnalysisService aiAnalysisService,
            PolicyEngine policyEngine
    ) {
        this.staticAnalyzers = staticAnalyzers;
        this.aiAnalysisService = aiAnalysisService;
        this.policyEngine = policyEngine;
    }

    public CodeValidationResponse review(CodeValidationRequest request) {
        CodeSubmission submission = new CodeSubmission(
                request.sourceCode(),
                request.filePath(),
                request.context(),
                defaultLanguage(request.language())
        );

        validateSubmission(submission);

        List<Issue> issues = new ArrayList<>();
        for (StaticAnalyzer analyzer : staticAnalyzers) {
            issues.addAll(analyzer.analyze(submission));
        }

        issues.addAll(aiAnalysisService.analyze(submission));

        DecisionResult decision = policyEngine.decide(issues);
        AnalysisSummary summary = new AnalysisSummary(
                issues,
                calculateRiskScore(issues),
                decision.status() == DecisionStatus.BLOCKED
        );

        return new CodeValidationResponse(
                decision,
                summary,
                buildSuggestions(issues)
        );
    }

    private void validateSubmission(CodeSubmission submission) {
        String source = submission.sourceCode();
        if (source == null || source.isBlank()) {
            throw new ValidationException("SOURCE_EMPTY", "소스 코드가 비어 있습니다.");
        }
        if (source.length() > MAX_SOURCE_LENGTH) {
            throw new ValidationException("SOURCE_TOO_LARGE", "소스 코드가 너무 큽니다.");
        }
        for (String pattern : FORBIDDEN_PATTERNS) {
            if (source.contains(pattern)) {
                throw new ValidationException("FORBIDDEN_PATTERN", "금지된 패턴이 포함되어 있습니다: " + pattern);
            }
        }
    }

    private String defaultLanguage(String language) {
        if (language == null || language.isBlank()) {
            return "JAVA";
        }
        return language.trim().toUpperCase();
    }

    private int calculateRiskScore(List<Issue> issues) {
        int score = 0;
        for (Issue issue : issues) {
            if (issue.severity() == IssueSeverity.CRITICAL) score += 25;
            if (issue.severity() == IssueSeverity.HIGH) score += 15;
            if (issue.severity() == IssueSeverity.MEDIUM) score += 8;
            if (issue.severity() == IssueSeverity.LOW) score += 3;
        }
        return Math.min(score, 100);
    }

    private List<String> buildSuggestions(List<Issue> issues) {
        List<String> suggestions = new ArrayList<>();
        boolean hasSecurity = issues.stream().anyMatch(i -> i.type() == IssueType.SECURITY);
        if (hasSecurity) {
            suggestions.add("보안 관련 API 사용 여부를 다시 확인하세요.");
        }
        boolean hasQuality = issues.stream().anyMatch(i -> i.type() == IssueType.QUALITY);
        if (hasQuality) {
            suggestions.add("가독성과 예외 처리 품질을 개선할 여지가 있습니다.");
        }
        if (suggestions.isEmpty()) {
            suggestions.add("특이 사항이 없습니다. 기본 품질 기준을 충족합니다.");
        }
        return suggestions;
    }
}
