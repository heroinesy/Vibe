package com.validator.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSchemaValidator {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonSchemaValidator() {
    }

    public static boolean isValidJson(String content) {
        try {
            JsonNode node = OBJECT_MAPPER.readTree(content);
            return node != null;
        } catch (Exception ex) {
            return false;
        }
    }
}
