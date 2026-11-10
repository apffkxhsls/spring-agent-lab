package org.sopt.springagentlab.infrastructure.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sopt.springagentlab.application.post.port.out.PostRepository;
import org.sopt.springagentlab.domain.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(PostPersistenceAdapter.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PostPersistenceAdapterTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostJpaRepository postJpaRepository;

    @Autowired
    private DataSource dataSource;

    @AfterEach
    void cleanUp() {
        postJpaRepository.deleteAll();
    }

    @ParameterizedTest
    @MethodSource("posts")
    void savesPostAndReturnsCommittedDatabaseId(Post post) {
        Long id = postRepository.save(post);

        assertNotNull(id);
        assertTrue(id > 0);

        // Read through JDBC after the adapter returns, bypassing the JPA cache.
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        var rows = jdbcTemplate.queryForList(
                "select id, title, content from posts where id = ?", id);

        assertEquals(1, rows.size());
        assertEquals(id, ((Number) rows.getFirst().get("ID")).longValue());
        assertEquals(post.getTitle(), rows.getFirst().get("TITLE"));
        assertEquals(post.getContent(), rows.getFirst().get("CONTENT"));
    }

    static Stream<Post> posts() {
        return Stream.of(
                new Post("  첫 게시글  ", "\t본문입니다.\r\n "),
                new Post(" " + "가".repeat(98) + " ", "\t" + "나".repeat(4_998) + "\n"));
    }
}
