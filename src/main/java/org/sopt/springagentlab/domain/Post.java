package org.sopt.springagentlab.domain;

public final class Post {

    public static final int MAX_TITLE_LENGTH = 100;
    public static final int MAX_CONTENT_LENGTH = 5_000;

    private final String title;
    private final String content;

    public Post(String title, String content) {
        validate(title, "제목", MAX_TITLE_LENGTH);
        validate(content, "본문", MAX_CONTENT_LENGTH);
        this.title = title;
        this.content = content;
    }

    private static void validate(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new InvalidPostException(field + "은 비어 있을 수 없습니다.");
        }
        if (value.length() > maxLength) {
            throw new InvalidPostException(field + "은 " + maxLength + "자 이하여야 합니다.");
        }
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
