package com.validator.infrastructure.ai;

import com.validator.domain.model.CodeSubmission;
import com.validator.domain.model.Issue;
import com.validator.exception.AiServiceException;
import com.validator.infrastructure.config.OpenAiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AiAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AiAnalysisService.class);

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
            logger.info("AI 분석이 비활성화되어 있습니다 (ai.openai.enabled=false).");
            return Collections.emptyList();
        }

        String prompt = buildPrompt(submission);
        try {
            logger.info("AI 분석 요청을 시작합니다.");
            String response = openAiClient.analyzeCode(prompt);
            List<Issue> issues = parser.parseIssues(response);
            logger.info("AI 분석이 완료되었습니다. issueCount={}", issues.size());
            return issues;
        } catch (AiServiceException ex) {
            logger.warn("AI 분석이 실패했습니다. code={}", ex.getCode());
            return Collections.emptyList();
        } catch (Exception ex) {
            logger.warn("AI 분석이 예기치 않게 실패했습니다.", ex);
            return Collections.emptyList();
        }
    }

    private String buildPrompt(CodeSubmission submission) {
        return """
                너는 코드 보안/품질 리뷰어다. 아래 코드를 분석하고 JSON 배열만 반환해라.
                출력 규칙:
                - 반드시 JSON 배열만 반환 (설명/마크다운/코드블록 금지)
                - 스키마: [{"type":"SECURITY|QUALITY|SYNTAX|PERFORMANCE|OTHER","severity":"LOW|MEDIUM|HIGH|CRITICAL","message":"...","line":<number|null>}]
                - line은 가능하면 숫자, 불가능하면 null
                - 최대 8개 이슈까지만 반환
                - 이슈가 없으면 [] 만 반환
                - message는 한국어로 작성
                입력 정보:
                - filePath: %s
                - language: %s
                - context: %s
                코드:
                %s
                """.formatted(
                        submission.filePath() == null ? "" : submission.filePath(),
                        submission.language() == null ? "" : submission.language(),
                        submission.context() == null ? "" : submission.context(),
                        submission.sourceCode()
                );
    }
}
