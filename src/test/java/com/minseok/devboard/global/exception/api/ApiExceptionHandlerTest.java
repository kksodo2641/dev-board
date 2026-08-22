package com.minseok.devboard.global.exception.api;

import com.minseok.devboard.board.exception.BoardNotFoundException;
import com.minseok.devboard.comment.exception.CommentNotFoundException;
import com.minseok.devboard.comment.exception.ReplyNotAllowedException;
import com.minseok.devboard.global.exception.AccessDeniedException;
import com.minseok.devboard.member.exception.MemberNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiExceptionHandlerTest {
    
    private MockMvc mockMvc;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestApiController())
                                 .setControllerAdvice(new ApiExceptionHandler())
                                 .build();
    }
    
    @ParameterizedTest(
            name = "[{index}] {4} -> HTTP {1}, errorCode {2}"
    )
    @DisplayName("도메인 및 권한 예외가 발생하면 예외에 대응하는 HTTP 상태와 ApiErrorCode를 응답한다.")
    @MethodSource("mappedExceptionCases")
    void returnMappedExceptionResponse(final String url,
                                       final int expectedStatus,
                                       final ApiErrorCode expectedErrorCode,
                                       final Class<? extends Exception> expectedExceptionType,
                                       final String exceptionDisplayName) throws Exception {
        final MvcResult mvcResult =
                mockMvc.perform(get(url))
                       .andExpect(status().is(expectedStatus))
                       .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                       .andExpect(jsonPath("$.code")
                                          .value(expectedErrorCode.getCode()))
                       .andExpect(jsonPath("$.message")
                                          .value(expectedErrorCode.getMessage()))
                       .andReturn();
        
        assertThat(mvcResult.getResolvedException())
                .isInstanceOf(expectedExceptionType);
    }
    
    @Test
    @DisplayName("요청 본문을 읽을 수 없으면 400 Bad Request와 INVALID_REQUEST를 응답한다.")
    void returnInvalidRequestBodyResponse() throws Exception {
        // given
        final String requestBody = """
                {
                    "content": "잘못된 JSON 형식"
                """;
        
        final ApiErrorCode expectedErrorCode = ApiErrorCode.INVALID_REQUEST;
        
        // when & then
        final MvcResult mvcResult =
                mockMvc.perform(post("/test/invalid-request-body")
                                        .contentType(APPLICATION_JSON)
                                        .content(requestBody))
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                       .andExpect(jsonPath("$.code")
                                          .value(expectedErrorCode.getCode()))
                       .andExpect(jsonPath("$.message")
                                          .value(expectedErrorCode.getMessage()))
                       .andReturn();
        
        assertThat(mvcResult.getResolvedException())
                .isInstanceOf(HttpMessageNotReadableException.class);
    }
    
    @Test
    @DisplayName("요청 값의 타입이 일치하지 않으면 400 Bad Request와 INVALID_REQUEST를 응답한다.")
    void returnTypeMismatchResponse() throws Exception {
        // given
        final ApiErrorCode expectedErrorCode = ApiErrorCode.INVALID_REQUEST;
        
        // when & then
        final MvcResult mvcResult =
                mockMvc.perform(get("/test/type-mismatch/abc"))
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                       .andExpect(jsonPath("$.code")
                                          .value(expectedErrorCode.getCode()))
                       .andExpect(jsonPath("$.message")
                                          .value(expectedErrorCode.getMessage()))
                       .andReturn();
        
        assertThat(mvcResult.getResolvedException())
                .isInstanceOf(MethodArgumentTypeMismatchException.class);
    }
    
    @Test
    @DisplayName("요청 값 검증에 실패하면 400 Bad Request, VALIDATION_ERROR와 검증 오류 메시지를 응답한다.")
    void returnValidationErrorResponse() throws Exception {
        // given
        final String requestBody = """
                {
                    "text": "   ",
                    "num": 123
                }
                """;
        
        final ApiErrorCode expectedErrorCode = ApiErrorCode.VALIDATION_ERROR;
        final String expectedErrorMessage = "공백은 허용되지 않습니다.";
        
        // when & then
        final MvcResult mvcResult =
                mockMvc.perform(post("/test/argument-not-valid")
                                        .contentType(APPLICATION_JSON)
                                        .content(requestBody))
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                       .andExpect(jsonPath("$.code")
                                          .value(expectedErrorCode.getCode()))
                       .andExpect(jsonPath("$.message")
                                          .value(expectedErrorMessage))
                       .andReturn();
        
        assertThat(mvcResult.getResolvedException())
                .isInstanceOf(MethodArgumentNotValidException.class);
    }
    
    @Test
    @DisplayName("검증 오류 메시지가 비어 있으면 400 Bad Request와 VALIDATION_ERROR의 기본 메시지를 응답한다.")
    void returnDefaultValidationErrorResponse() throws Exception {
        // given
        final String requestBody = """
                {
                    "text": "   "
                }
                """;
        
        final ApiErrorCode expectedErrorCode = ApiErrorCode.VALIDATION_ERROR;
        
        // when & then
        final MvcResult mvcResult =
                mockMvc.perform(post("/test/argument-not-valid-no-message")
                                        .contentType(APPLICATION_JSON)
                                        .content(requestBody))
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                       .andExpect(jsonPath("$.code")
                                          .value(expectedErrorCode.getCode()))
                       .andExpect(jsonPath("$.message")
                                          .value(expectedErrorCode.getMessage()))
                       .andReturn();
        
        assertThat(mvcResult.getResolvedException())
                .isInstanceOf(MethodArgumentNotValidException.class);
    }
    
    @Test
    @DisplayName("예상하지 못한 예외가 발생하면 500 Internal Server Error와 INTERNAL_SERVER_ERROR를 응답한다.")
    void returnInternalServerErrorResponse() throws Exception {
        // given
        final ApiErrorCode expectedErrorCode = ApiErrorCode.INTERNAL_SERVER_ERROR;
        
        // when & then
        final MvcResult mvcResult =
                mockMvc.perform(get("/test/unhandled-exception"))
                       .andExpect(status().isInternalServerError())
                       .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                       .andExpect(jsonPath("$.code")
                                          .value(expectedErrorCode.getCode()))
                       .andExpect(jsonPath("$.message")
                                          .value(expectedErrorCode.getMessage()))
                       .andReturn();
        
        assertThat(mvcResult.getResolvedException())
                .isInstanceOf(RuntimeException.class);
    }
    
    private static Stream<Arguments> mappedExceptionCases() {
        return Stream.of(
                Arguments.of(
                        "/test/member-not-found",
                        NOT_FOUND.value(),
                        ApiErrorCode.MEMBER_NOT_FOUND,
                        MemberNotFoundException.class,
                        "MemberNotFoundException"),
                Arguments.of(
                        "/test/board-not-found",
                        NOT_FOUND.value(),
                        ApiErrorCode.BOARD_NOT_FOUND,
                        BoardNotFoundException.class,
                        "BoardNotFoundException"),
                Arguments.of(
                        "/test/comment-not-found",
                        NOT_FOUND.value(),
                        ApiErrorCode.COMMENT_NOT_FOUND,
                        CommentNotFoundException.class,
                        "CommentNotFoundException"),
                Arguments.of(
                        "/test/reply-not-allowed",
                        CONFLICT.value(),
                        ApiErrorCode.REPLY_NOT_ALLOWED,
                        ReplyNotAllowedException.class,
                        "ReplyNotAllowedException"),
                Arguments.of(
                        "/test/access-denied",
                        FORBIDDEN.value(),
                        ApiErrorCode.ACCESS_DENIED,
                        AccessDeniedException.class,
                        "AccessDeniedException")
        );
    }
    
    @RestController
    @RequestMapping("/test")
    static class TestApiController {
        
        @GetMapping("/member-not-found")
        public void throwMemberNotFound() {
            throw new MemberNotFoundException();
        }
        
        @GetMapping("/board-not-found")
        public void throwBoardNotFound() {
            throw new BoardNotFoundException();
        }
        
        @GetMapping("/comment-not-found")
        public void throwCommentNotFound() {
            throw new CommentNotFoundException();
        }
        
        @GetMapping("/reply-not-allowed")
        public void throwReplyNotAllowed() {
            throw new ReplyNotAllowedException();
        }
        
        @GetMapping("/access-denied")
        public void throwAccessDenied() {
            throw new AccessDeniedException();
        }
        
        @PostMapping("/invalid-request-body")
        public void invalidRequestBody(final @RequestBody TestRequest request) {
        }
        
        @GetMapping("/type-mismatch/{num}")
        public void typeMismatch(final @PathVariable("num") int num) {
        }
        
        @PostMapping("/argument-not-valid")
        public void argumentNotValid(final @Valid @RequestBody TestDto dto) {
        }
        
        @PostMapping("/argument-not-valid-no-message")
        public void argumentNotValidWithNoMessage(final @Valid @RequestBody TestDtoNoMessage dto) {
        }
        
        @GetMapping("/unhandled-exception")
        public void throwRuntimeException() {
            throw new RuntimeException();
        }
        
        record TestRequest(String content) {
        }
        
        @NoArgsConstructor
        @AllArgsConstructor
        static class TestDto {
            
            @NotBlank(message = "공백은 허용되지 않습니다.")
            private String text;
            
            @NotNull(message = "필수값입니다.")
            private Integer num;
        }
        
        @NoArgsConstructor
        @AllArgsConstructor
        static class TestDtoNoMessage {
            
            @NotBlank(message = "")
            private String text;
        }
    }
}
