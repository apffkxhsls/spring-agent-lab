package org.sopt.springagentlab.application.post.service;

import org.sopt.springagentlab.application.post.port.in.CreatePostCommand;
import org.sopt.springagentlab.application.post.port.in.CreatePostUseCase;
import org.sopt.springagentlab.application.post.port.out.PostRepository;
import org.sopt.springagentlab.domain.Post;

public class CreatePostService implements CreatePostUseCase {

    private final PostRepository postRepository;

    public CreatePostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public Long createPost(CreatePostCommand command) {
        Post post = new Post(command.title(), command.content());
        return postRepository.save(post);
    }
}
