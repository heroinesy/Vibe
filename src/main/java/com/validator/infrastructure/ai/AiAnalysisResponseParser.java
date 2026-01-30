package com.validator.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.validator.domain.model.Issue;
import com.validator.domain.model.IssueSeverity;
import com.validator.domain.model.IssueSource;
import com.validator.domain.model.IssueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AiAnalysisResponseParser {

    private static final Logger logger = LoggerFactory.getLogger(AiAnalysisResponseParser.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Issue> parseIssues(String responseContent) {
        List<Issue> issues = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseContent);
            if (root.isArray()) {
                return parseIssueArray(root, issues);
            }

            JsonNode message = root.at("/choices/0/message/content");
            if (message.isMissingNode()) {
                logger.warn("AI 응답에 message content가 없습니다.");
                return issues;
            }

            String content = message.asText();
            JsonNode parsed = objectMapper.readTree(content);
            if (parsed.isArray()) {
                return parseIssueArray(parsed, issues);
            }
            String extracted = extractJsonArray(content);
            if (extracted != null) {
                JsonNode extractedNode = objectMapper.readTree(extracted);
                if (extractedNode.isArray()) {
                    return parseIssueArray(extractedNode, issues);
                }
            }
            logger.warn("AI 응답 content가 JSON 배열 형식이 아닙니다.");
        } catch (Exception ignored) {
            return issues;
        }
        return issues;
    }

    private List<Issue> parseIssueArray(JsonNode arrayNode, List<Issue> issues) {
        for (JsonNode node : arrayNode) {
            IssueType type = safeType(node.path("type").asText(null));
            IssueSeverity severity = safeSeverity(node.path("severity").asText(null));
            String messageText = node.path("message").asText(null);

            if (type == null || severity == null || messageText == null || messageText.isBlank()) {
                continue;
            }

            Integer line = node.path("line").isInt() ? node.path("line").asInt() : null;
            issues.add(new Issue(type, severity, messageText, line, IssueSource.AI_ASSIST));
        }
        return issues;
    }

    private String extractJsonArray(String content) {
        int start = content.indexOf('[');
        int end = content.lastIndexOf(']');
        if (start == -1 || end == -1 || end <= start) {
            return null;
        }
        return content.substring(start, end + 1);
    }

    private IssueType safeType(String raw) {
        if (raw == null) return null;
        try {
            return IssueType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return IssueType.OTHER;
        }
    }

    private IssueSeverity safeSeverity(String raw) {
        if (raw == null) return null;
        try {
            return IssueSeverity.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return IssueSeverity.LOW;
        }
    }
}
