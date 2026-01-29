package com.validator.infrastructure.analyzer;

import com.validator.domain.model.CodeSubmission;
import com.validator.domain.model.Issue;

import java.util.List;

public interface StaticAnalyzer {
    List<Issue> analyze(CodeSubmission submission);
}
