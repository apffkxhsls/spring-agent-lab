package org.sopt.springagentlab;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sopt.springagentlab.presentation.InvalidPostRequests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.datasource.url=jdbc:h2:mem:post-http-test;DB_CLOSE_DELAY=-1",
                "spring.jpa.hibernate.ddl-auto=create-drop"})
class CreatePostIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("delete from posts");
    }

    @ParameterizedTest
    @MethodSource("validInputs")
    void createsPostThroughHttpAndCommitsToDatabase(String title, String content) throws Exception {
        HttpResponse<String> response = send(InvalidPostRequests.json(title, content));

        assertEquals(201, response.statusCode());
        var body = new JsonMapper().readTree(response.body());
        assertTrue(body.path("id").isIntegralNumber());
        long id = body.path("id").asLong();
        assertTrue(id > 0);
        var rows = jdbcTemplate.queryForList("select title, content from posts where id = ?", id);
        assertEquals(1, rows.size());
        assertEquals(title, rows.getFirst().get("TITLE"));
        assertEquals(content, rows.getFirst().get("CONTENT"));
        assertEquals(1L, jdbcTemplate.queryForObject("select count(*) from posts", Long.class));
    }

    @ParameterizedTest
    @MethodSource("org.sopt.springagentlab.presentation.InvalidPostRequests#requests")
    void invalidRequestDoesNotAddData(String request) throws Exception {
        // Keep an existing row to verify that invalid input leaves data unchanged.
        jdbcTemplate.update("insert into posts (title, content) values (?, ?)", "기존 제목", "기존 본문");

        HttpResponse<String> response = send(request);

        assertEquals(400, response.statusCode());
        assertFalse(new JsonMapper().readTree(response.body()).path("message").asText().isBlank());
        assertEquals(1L, jdbcTemplate.queryForObject("select count(*) from posts", Long.class));
        assertEquals("기존 제목", jdbcTemplate.queryForObject("select title from posts", String.class));
    }

    private HttpResponse<String> send(String body) throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/posts"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
        }
    }

    static Stream<Arguments> validInputs() {
        return Stream.of(Arguments.of("  제목  ", "\t본문\r\n "),
                Arguments.of(" " + "가".repeat(98) + " ", "\t" + "나".repeat(4_998) + "\n"));
    }
}
