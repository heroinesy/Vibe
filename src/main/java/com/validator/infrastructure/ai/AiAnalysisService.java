package com.validator.infrastructure.ai;

import com.validator.domain.model.CodeSubmission;
import com.validator.domain.model.Issue;
import com.validator.exception.AiServiceException;
import com.validator.infrastructure.config.OpenAiProperties;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AiAnalysisService {

    private final OpenAiClient openAiClient;
    private final AiAnalysisResponseParser parser;
    private final OpenAiProperties properties;

    public AiAnalysisService(
            OpenAiClient openAiClient,
            AiAnalysisResponseParser parser,
            OpenAiProperties properties
    ) {
        this.openAiClient = openAiClient;
        this.parser = parser;
        this.properties = properties;
    }

    public List<Issue> analyze(CodeSubmission submission) {
        if (!properties.isEnabled()) {
            return Collections.emptyList();
        }

        String prompt = buildPrompt(submission);
        try {
            String response = openAiClient.analyzeCode(prompt);
            return parser.parseIssues(response);
        } catch (AiServiceException ex) {
            return Collections.emptyList();
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private String buildPrompt(CodeSubmission submission) {
        return """
                다음 Java 코드를 보안/품질 관점에서 분석하고 JSON 배열로만 응답하세요.
                스키마: [{"type":"SECURITY|QUALITY|SYNTAX|PERFORMANCE|OTHER","severity":"LOW|MEDIUM|HIGH|CRITICAL","message":"...","line":<number>}]
                코드:
                %s
                """.formatted(submission.sourceCode());
    }
}
