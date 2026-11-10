package org.sopt.springagentlab.presentation;

import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class InvalidPostRequests {

    private InvalidPostRequests() {
    }

    public static Stream<String> requests() {
        Stream<String> invalidFields = Stream.of(null, "", " ", "\t\n", "가".repeat(101))
                .map(title -> json(title, "본문"));
        Stream<String> invalidContents = Stream.of(null, "", " ", "\t\n", "나".repeat(5_001))
                .map(content -> json("제목", content));
        return Stream.concat(Stream.concat(invalidFields, invalidContents),
                Stream.of("{}", "{\"title\":\"제목\"}", "{\"content\":\"본문\"}", "", "{", "null"));
    }

    public static String json(String title, String content) {
        Map<String, String> values = new HashMap<>();
        values.put("title", title);
        values.put("content", content);
        return new JsonMapper().writeValueAsString(values);
    }
}
