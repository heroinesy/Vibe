package com.validator.infrastructure.analyzer;

import com.validator.domain.model.CodeSubmission;
import com.validator.domain.model.Issue;
import com.validator.domain.model.IssueSeverity;
import com.validator.domain.model.IssueSource;
import com.validator.domain.model.IssueType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class QualityValidator implements StaticAnalyzer {

    @Override
    public List<Issue> analyze(CodeSubmission submission) {
        List<Issue> issues = new ArrayList<>();
        String code = submission.sourceCode();

        if (code.contains("System.out.println")) {
            issues.add(new Issue(
                    IssueType.QUALITY,
                    IssueSeverity.LOW,
                    "System.out.println 사용은 로깅 프레임워크로 교체하세요.",
                    null,
                    IssueSource.STATIC_ANALYZER
            ));
        }

        if (code.contains("TODO")) {
            issues.add(new Issue(
                    IssueType.QUALITY,
                    IssueSeverity.MEDIUM,
                    "TODO 주석이 남아 있습니다.",
                    null,
                    IssueSource.STATIC_ANALYZER
            ));
        }

        if (code.contains("catch (Exception") && !code.contains("log.")) {
            issues.add(new Issue(
                    IssueType.QUALITY,
                    IssueSeverity.MEDIUM,
                    "예외 처리 시 로깅이나 상세 처리가 필요합니다.",
                    null,
                    IssueSource.STATIC_ANALYZER
            ));
        }

        return issues;
    }
}
