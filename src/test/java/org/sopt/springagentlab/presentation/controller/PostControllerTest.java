package org.sopt.springagentlab.presentation.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sopt.springagentlab.application.post.port.in.CreatePostCommand;
import org.sopt.springagentlab.application.post.port.in.CreatePostUseCase;
import org.sopt.springagentlab.domain.InvalidPostException;
import org.sopt.springagentlab.presentation.InvalidPostRequests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreatePostUseCase createPostUseCase;

    @Test
    void returnsCreatedIdAndPreservesInput() throws Exception {
        CreatePostCommand command = new CreatePostCommand("  제목  ", "\t본문\n");
        when(createPostUseCase.createPost(command)).thenReturn(42L);

        mockMvc.perform(post("/api/v1/posts").contentType(MediaType.APPLICATION_JSON)
                        .content(InvalidPostRequests.json(command.title(), command.content())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42));

        verify(createPostUseCase).createPost(command);
        verifyNoMoreInteractions(createPostUseCase);
    }

    @ParameterizedTest
    @MethodSource("org.sopt.springagentlab.presentation.InvalidPostRequests#requests")
    void rejectsInvalidRequestWithoutCallingUseCase(String body) throws Exception {
        mockMvc.perform(post("/api/v1/posts").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", not(isEmptyOrNullString())));

        verifyNoInteractions(createPostUseCase);
    }

    @Test
    void combinesFieldErrorsWithoutDependingOnOrder() throws Exception {
        mockMvc.perform(post("/api/v1/posts").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", allOf(
                        containsString("제목은 비어 있을 수 없습니다."),
                        containsString("본문은 비어 있을 수 없습니다."))));

        verifyNoInteractions(createPostUseCase);
    }

    @Test
    void translatesDomainExceptionToBadRequest() throws Exception {
        CreatePostCommand command = new CreatePostCommand("제목", "본문");
        when(createPostUseCase.createPost(command)).thenThrow(new InvalidPostException("도메인 검증 실패"));

        mockMvc.perform(post("/api/v1/posts").contentType(MediaType.APPLICATION_JSON)
                        .content(InvalidPostRequests.json(command.title(), command.content())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("도메인 검증 실패"));
    }
}
