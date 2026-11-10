package org.sopt.springagentlab.presentation.controller;

import jakarta.validation.Valid;
import org.sopt.springagentlab.application.post.port.in.CreatePostUseCase;
import org.sopt.springagentlab.presentation.dto.CreatePostRequest;
import org.sopt.springagentlab.presentation.dto.CreatePostResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostController {

    private final CreatePostUseCase createPostUseCase;

    public PostController(CreatePostUseCase createPostUseCase) {
        this.createPostUseCase = createPostUseCase;
    }

    @PostMapping("/api/v1/posts")
    public ResponseEntity<CreatePostResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
        Long id = createPostUseCase.createPost(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreatePostResponse(id));
    }
}
