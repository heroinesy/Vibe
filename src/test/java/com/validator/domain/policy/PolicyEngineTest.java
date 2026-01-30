package com.validator.domain.policy;

import com.validator.domain.model.DecisionStatus;
import com.validator.domain.model.Issue;
import com.validator.domain.model.IssueSeverity;
import com.validator.domain.model.IssueSource;
import com.validator.domain.model.IssueType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyEngineTest {

    @Test
    void blocksWhenCriticalSecurityIssueExists() {
        PolicyEngine engine = new PolicyEngine();
        List<Issue> issues = List.of(
                issue(IssueType.SECURITY, IssueSeverity.CRITICAL)
        );

        var result = engine.decide(issues);

        assertThat(result.status()).isEqualTo(DecisionStatus.BLOCKED);
    }

    @Test
    void blocksWhenHighSecurityIssueExists() {
        PolicyEngine engine = new PolicyEngine();
        List<Issue> issues = List.of(
                issue(IssueType.SECURITY, IssueSeverity.HIGH)
        );

        var result = engine.decide(issues);

        assertThat(result.status()).isEqualTo(DecisionStatus.BLOCKED);
    }

    @Test
    void blocksWhenMultipleHighIssuesExist() {
        PolicyEngine engine = new PolicyEngine();
        List<Issue> issues = List.of(
                issue(IssueType.QUALITY, IssueSeverity.HIGH),
                issue(IssueType.SYNTAX, IssueSeverity.HIGH)
        );

        var result = engine.decide(issues);

        assertThat(result.status()).isEqualTo(DecisionStatus.BLOCKED);
    }

    @Test
    void warnsWhenMultipleSecurityMediumIssuesExist() {
        PolicyEngine engine = new PolicyEngine();
        List<Issue> issues = List.of(
                issue(IssueType.SECURITY, IssueSeverity.MEDIUM),
                issue(IssueType.SECURITY, IssueSeverity.MEDIUM)
        );

        var result = engine.decide(issues);

        assertThat(result.status()).isEqualTo(DecisionStatus.WARN_ONLY);
    }

    @Test
    void warnsWhenMultipleQualityMediumIssuesExist() {
        PolicyEngine engine = new PolicyEngine();
        List<Issue> issues = List.of(
                issue(IssueType.QUALITY, IssueSeverity.MEDIUM),
                issue(IssueType.QUALITY, IssueSeverity.MEDIUM)
        );

        var result = engine.decide(issues);

        assertThat(result.status()).isEqualTo(DecisionStatus.WARN_ONLY);
    }

    @Test
    void allowsWhenNoIssuesExist() {
        PolicyEngine engine = new PolicyEngine();

        var result = engine.decide(List.of());

        assertThat(result.status()).isEqualTo(DecisionStatus.ALLOWED);
    }

    private Issue issue(IssueType type, IssueSeverity severity) {
        return new Issue(type, severity, "test", null, IssueSource.STATIC_ANALYZER);
    }
}
