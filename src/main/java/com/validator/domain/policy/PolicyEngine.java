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
                    "HIGH_ISSUES_BLOCK",
                    "Block too many high issues",
                    IssueType.ANY,
                    IssueSeverity.HIGH,
                    3,
                    PolicyAction.BLOCK
            ),
            new PolicyRule(
                    "MEDIUM_ISSUES_WARN",
                    "Warn on many medium issues",
                    IssueType.ANY,
                    IssueSeverity.MEDIUM,
                    5,
                    PolicyAction.WARN
            )
    );

    public DecisionResult decide(List<Issue> issues) {
        List<String> triggered = new ArrayList<>();

        DecisionStatus status = DecisionStatus.ALLOWED;
        String reason = "No blocking issues found";

        for (PolicyRule rule : rules) {
            int count = countMatching(issues, rule);
            if (count >= rule.minCount()) {
                triggered.add(rule.id());
                if (rule.action() == PolicyAction.BLOCK) {
                    status = DecisionStatus.BLOCKED;
                    reason = rule.name();
                    break;
                }
                if (rule.action() == PolicyAction.WARN && status != DecisionStatus.BLOCKED) {
                    status = DecisionStatus.WARN_ONLY;
                    reason = rule.name();
                }
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
}
