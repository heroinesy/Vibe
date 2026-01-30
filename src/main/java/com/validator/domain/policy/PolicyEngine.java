package com.validator.domain.policy;

import com.validator.domain.model.DecisionResult;
import com.validator.domain.model.DecisionStatus;
import com.validator.domain.model.Issue;
import com.validator.domain.model.IssueSeverity;
import com.validator.domain.model.IssueType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PolicyEngine {

    private final List<PolicyRule> rules = List.of(
            new PolicyRule(
                    "SECURITY_CRITICAL_BLOCK",
                    "Block critical security issues",
                    IssueType.SECURITY,
                    IssueSeverity.CRITICAL,
                    1,
                    PolicyAction.BLOCK
            ),
            new PolicyRule(
                    "SECURITY_HIGH_BLOCK",
                    "Block high severity security issues",
                    IssueType.SECURITY,
                    IssueSeverity.HIGH,
                    1,
                    PolicyAction.BLOCK
            ),
            new PolicyRule(
                    "CRITICAL_ISSUES_BLOCK",
                    "Block critical issues",
                    IssueType.ANY,
                    IssueSeverity.CRITICAL,
                    1,
                    PolicyAction.BLOCK
            ),
            new PolicyRule(
                    "HIGH_ISSUES_BLOCK",
                    "Block too many high issues",
                    IssueType.ANY,
                    IssueSeverity.HIGH,
                    2,
                    PolicyAction.BLOCK
            ),
            new PolicyRule(
                    "SECURITY_MEDIUM_WARN",
                    "Warn on multiple security issues",
                    IssueType.SECURITY,
                    IssueSeverity.MEDIUM,
                    2,
                    PolicyAction.WARN
            ),
            new PolicyRule(
                    "MEDIUM_ISSUES_WARN",
                    "Warn on many medium issues",
                    IssueType.ANY,
                    IssueSeverity.MEDIUM,
                    5,
                    PolicyAction.WARN
            ),
            new PolicyRule(
                    "QUALITY_MEDIUM_WARN",
                    "Warn on multiple quality issues",
                    IssueType.QUALITY,
                    IssueSeverity.MEDIUM,
                    2,
                    PolicyAction.WARN
            )
    );

    public DecisionResult decide(List<Issue> issues) {
        List<RuleMatch> matches = new ArrayList<>();

        DecisionStatus status = DecisionStatus.ALLOWED;
        String reason = "No blocking issues found";

        for (PolicyRule rule : rules) {
            int count = countMatching(issues, rule);
            if (count >= rule.minCount()) {
                matches.add(new RuleMatch(rule, count));
            }
        }

        List<String> triggered = matches.stream()
                .map(match -> match.rule().id())
                .toList();

        RuleMatch blockMatch = matches.stream()
                .filter(match -> match.rule().action() == PolicyAction.BLOCK)
                .findFirst()
                .orElse(null);
        if (blockMatch != null) {
            status = DecisionStatus.BLOCKED;
            reason = blockMatch.rule().name() + " (count=" + blockMatch.count() + ")";
        } else {
            RuleMatch warnMatch = matches.stream()
                    .filter(match -> match.rule().action() == PolicyAction.WARN)
                    .findFirst()
                    .orElse(null);
            if (warnMatch != null) {
                status = DecisionStatus.WARN_ONLY;
                reason = warnMatch.rule().name() + " (count=" + warnMatch.count() + ")";
            }
        }

        return new DecisionResult(status, reason, triggered);
    }

    private int countMatching(List<Issue> issues, PolicyRule rule) {
        if (rule.type() == IssueType.ANY) {
            return (int) issues.stream()
                    .filter(issue -> issue.severity().isAtLeast(rule.minSeverity()))
                    .count();
        }
        return (int) issues.stream()
                .filter(issue -> issue.type() == rule.type())
                .filter(issue -> issue.severity().isAtLeast(rule.minSeverity()))
                .count();
    }

    private record RuleMatch(PolicyRule rule, int count) {
    }
}
