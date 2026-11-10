package org.sopt.springagentlab.configuration;

import org.sopt.springagentlab.application.post.port.in.CreatePostUseCase;
import org.sopt.springagentlab.application.post.port.out.PostRepository;
import org.sopt.springagentlab.application.post.service.CreatePostService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PostConfiguration {

    @Bean
    public CreatePostUseCase createPostUseCase(PostRepository postRepository) {
        return new CreatePostService(postRepository);
    }
}
