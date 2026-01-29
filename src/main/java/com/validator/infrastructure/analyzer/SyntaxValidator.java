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
public class SyntaxValidator implements StaticAnalyzer {

    @Override
    public List<Issue> analyze(CodeSubmission submission) {
        List<Issue> issues = new ArrayList<>();
        String code = submission.sourceCode();

        int openBraces = 0;
        for (char c : code.toCharArray()) {
            if (c == '{') openBraces++;
            if (c == '}') openBraces--;
        }
        if (openBraces != 0) {
            issues.add(new Issue(
                    IssueType.SYNTAX,
                    IssueSeverity.HIGH,
                    "중괄호 개수가 일치하지 않습니다.",
                    null,
                    IssueSource.STATIC_ANALYZER
            ));
        }

        if (!code.contains("class ")) {
            issues.add(new Issue(
                    IssueType.SYNTAX,
                    IssueSeverity.MEDIUM,
                    "클래스 정의가 감지되지 않습니다.",
                    null,
                    IssueSource.STATIC_ANALYZER
            ));
        }

        return issues;
    }
}
