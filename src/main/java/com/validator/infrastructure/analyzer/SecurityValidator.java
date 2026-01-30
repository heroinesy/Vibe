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

        if (code.contains("MessageDigest.getInstance(\"MD5\")") || code.contains("MessageDigest.getInstance(\"SHA1\")")) {
            issues.add(new Issue(
                    IssueType.SECURITY,
                    IssueSeverity.MEDIUM,
                    "취약한 해시 알고리즘 사용이 감지되었습니다.",
                    null,
                    IssueSource.STATIC_ANALYZER
            ));
        }

        if (code.contains("http://")) {
            issues.add(new Issue(
                    IssueType.SECURITY,
                    IssueSeverity.LOW,
                    "HTTP 사용은 평문 전송 위험이 있습니다.",
                    null,
                    IssueSource.STATIC_ANALYZER
            ));
        }

        List<String> secretPatterns = List.of(
                "password=\"",
                "password = \"",
                "apiKey=\"",
                "apiKey = \"",
                "secret=\"",
                "secret = \"",
                "token=\"",
                "token = \""
        );
        for (String pattern : secretPatterns) {
            if (code.contains(pattern)) {
                issues.add(new Issue(
                        IssueType.SECURITY,
                        IssueSeverity.HIGH,
                        "하드코딩된 시크릿이 의심됩니다.",
                        null,
                        IssueSource.STATIC_ANALYZER
                ));
                break;
            }
        }

        return issues;
    }
}
