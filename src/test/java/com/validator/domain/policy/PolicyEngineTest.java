package com.validator.domain.policy;

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
                new Issue(IssueType.SECURITY, IssueSeverity.CRITICAL, "exec 사용", null, IssueSource.STATIC_ANALYZER)
        );

        var result = engine.decide(issues);

        assertThat(result.status().name()).isEqualTo("BLOCKED");
    }
}
