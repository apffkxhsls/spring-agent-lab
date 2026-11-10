package org.sopt.springagentlab.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.sopt.springagentlab.application.post.port.in.CreatePostCommand;

public record CreatePostRequest(
        @NotBlank(message = "제목은 비어 있을 수 없습니다.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.") String title,
        @NotBlank(message = "본문은 비어 있을 수 없습니다.")
        @Size(max = 5_000, message = "본문은 5000자 이하여야 합니다.") String content) {

    public CreatePostCommand toCommand() {
        return new CreatePostCommand(title, content);
    }
}
