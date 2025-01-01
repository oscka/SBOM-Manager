package com.osckorea.sbommanager.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class Util {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String extractMetadataComponentName(String jsonString) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            return rootNode.path("metadata")
                    .path("component")
                    .path("name")
                    .asText("");
        } catch (IOException e) {
            throw new RuntimeException("Error parsing JSON", e);
        }
    }

    public static void test(String jsonString) throws Exception {
        Object jsonObject = objectMapper.readValue(jsonString, Object.class);
        // 다시 JSON 문자열로 변환 (이 과정에서 유효성 검사가 이루어짐)
        String validJsonString = objectMapper.writeValueAsString(jsonObject);
    }
}
