package org.sopt.springagentlab.application.post.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sopt.springagentlab.application.post.port.in.CreatePostCommand;
import org.sopt.springagentlab.application.post.port.in.CreatePostUseCase;
import org.sopt.springagentlab.application.post.port.out.PostRepository;
import org.sopt.springagentlab.domain.InvalidPostException;
import org.sopt.springagentlab.domain.Post;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreatePostServiceTest {

    @Test
    void savesPostOnceAndReturnsGeneratedId() {
        RecordingPostRepository repository = new RecordingPostRepository();
        CreatePostUseCase useCase = new CreatePostService(repository);

        Long id = useCase.createPost(new CreatePostCommand("  제목  ", "\t본문\n"));

        assertEquals(1, repository.saveCount);
        assertEquals("  제목  ", repository.savedPost.getTitle());
        assertEquals("\t본문\n", repository.savedPost.getContent());
        assertEquals(Long.valueOf(42L), id);
    }

    @Test
    void savesMaximumLengthInput() {
        RecordingPostRepository repository = new RecordingPostRepository();
        CreatePostUseCase useCase = new CreatePostService(repository);
        String title = "가".repeat(100);
        String content = "나".repeat(5_000);

        Long id = useCase.createPost(new CreatePostCommand(title, content));

        assertEquals(1, repository.saveCount);
        assertEquals(title, repository.savedPost.getTitle());
        assertEquals(content, repository.savedPost.getContent());
        assertEquals(Long.valueOf(42L), id);
    }

    @ParameterizedTest
    @MethodSource("invalidCommands")
    void doesNotCallRepositoryWhenDomainValidationFails(CreatePostCommand command) {
        RecordingPostRepository repository = new RecordingPostRepository();
        CreatePostUseCase useCase = new CreatePostService(repository);

        assertThrows(InvalidPostException.class, () -> useCase.createPost(command));

        assertEquals(0, repository.saveCount);
    }

    static Stream<CreatePostCommand> invalidCommands() {
        Stream<CreatePostCommand> blankInputs = Stream.<String>of(null, "", " ", "\t", "\n", " \t\r\n ")
                .flatMap(value -> Stream.of(
                        new CreatePostCommand(value, "본문"),
                        new CreatePostCommand("제목", value)));
        return Stream.concat(blankInputs, Stream.of(
                new CreatePostCommand("가".repeat(101), "본문"),
                new CreatePostCommand("제목", "나".repeat(5_001)),
                new CreatePostCommand(" " + "가".repeat(99) + " ", "본문"),
                new CreatePostCommand("제목", " " + "나".repeat(4_999) + " ")));
    }

    private static class RecordingPostRepository implements PostRepository {

        private int saveCount;
        private Post savedPost;

        @Override
        public Long save(Post post) {
            saveCount++;
            savedPost = post;
            return 42L;
        }
    }
}
