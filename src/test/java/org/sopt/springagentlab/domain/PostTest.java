package org.sopt.springagentlab.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostTest {

    @Test
    void createsPostPreservingWhitespace() {
        Post post = new Post("  첫 번째 게시글  ", "\t본문입니다.\n");

        assertEquals("  첫 번째 게시글  ", post.getTitle());
        assertEquals("\t본문입니다.\n", post.getContent());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n", " \t\r\n "})
    void rejectsBlankTitle(String title) {
        InvalidPostException exception = assertThrows(InvalidPostException.class,
                () -> new Post(title, "본문"));

        assertTrue(exception.getMessage().contains("제목"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n", " \t\r\n "})
    void rejectsBlankContent(String content) {
        InvalidPostException exception = assertThrows(InvalidPostException.class,
                () -> new Post("제목", content));

        assertTrue(exception.getMessage().contains("본문"));
    }

    @Test
    void acceptsMinimumLength() {
        Post post = new Post("가", "나");

        assertEquals("가", post.getTitle());
        assertEquals("나", post.getContent());
    }

    @Test
    void acceptsMaximumLengths() {
        String title = "가".repeat(100);
        String content = "나".repeat(5_000);

        Post post = new Post(title, content);

        assertEquals(title, post.getTitle());
        assertEquals(content, post.getContent());
    }

    @Test
    void rejectsTitleOverMaximumLength() {
        InvalidPostException exception = assertThrows(InvalidPostException.class,
                () -> new Post("가".repeat(101), "본문"));

        assertTrue(exception.getMessage().contains("제목"));
        assertTrue(exception.getMessage().contains("100"));
    }

    @Test
    void rejectsContentOverMaximumLength() {
        InvalidPostException exception = assertThrows(InvalidPostException.class,
                () -> new Post("제목", "나".repeat(5_001)));

        assertTrue(exception.getMessage().contains("본문"));
        assertTrue(exception.getMessage().contains("5000"));
    }

    @Test
    void includesSurroundingWhitespaceInLength() {
        assertThrows(InvalidPostException.class,
                () -> new Post(" " + "가".repeat(99) + " ", "본문"));
        assertThrows(InvalidPostException.class,
                () -> new Post("제목", " " + "나".repeat(4_999) + " "));
    }
}
