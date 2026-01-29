package com.validator.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.validator.domain.model.Issue;
import com.validator.domain.model.IssueSeverity;
import com.validator.domain.model.IssueSource;
import com.validator.domain.model.IssueType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AiAnalysisResponseParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Issue> parseIssues(String responseContent) {
        List<Issue> issues = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseContent);
            JsonNode message = root.at("/choices/0/message/content");
            if (message.isMissingNode()) {
                return issues;
            }

            JsonNode parsed = objectMapper.readTree(message.asText());
            if (!parsed.isArray()) {
                return issues;
            }

            for (JsonNode node : parsed) {
                IssueType type = safeType(node.path("type").asText(null));
                IssueSeverity severity = safeSeverity(node.path("severity").asText(null));
                String messageText = node.path("message").asText(null);

                if (type == null || severity == null || messageText == null || messageText.isBlank()) {
                    continue;
                }

                Integer line = node.path("line").isInt() ? node.path("line").asInt() : null;
                issues.add(new Issue(type, severity, messageText, line, IssueSource.AI_ASSIST));
            }
        } catch (Exception ignored) {
            return issues;
        }
        return issues;
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
