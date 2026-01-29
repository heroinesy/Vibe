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
public class SecurityValidator implements StaticAnalyzer {

    @Override
    public List<Issue> analyze(CodeSubmission submission) {
        List<Issue> issues = new ArrayList<>();
        String code = submission.sourceCode();

        if (code.contains("Runtime.getRuntime().exec")) {
            issues.add(new Issue(
                    IssueType.SECURITY,
                    IssueSeverity.CRITICAL,
                    "Runtime.exec 사용은 명령 주입 위험이 있습니다.",
                    null,
                    IssueSource.STATIC_ANALYZER
            ));
        }

        if (code.contains("ProcessBuilder")) {
            issues.add(new Issue(
                    IssueType.SECURITY,
                    IssueSeverity.HIGH,
                    "ProcessBuilder 사용 시 입력 검증이 필요합니다.",
                    null,
                    IssueSource.STATIC_ANALYZER
            ));
        }

        if (code.contains("javax.crypto") && code.contains("Cipher.getInstance(\"AES\")")) {
            issues.add(new Issue(
                    IssueType.SECURITY,
                    IssueSeverity.MEDIUM,
                    "암호화 모드/패딩 명시가 필요할 수 있습니다.",
                    null,
                    IssueSource.STATIC_ANALYZER
            ));
        }

        return issues;
    }
}
