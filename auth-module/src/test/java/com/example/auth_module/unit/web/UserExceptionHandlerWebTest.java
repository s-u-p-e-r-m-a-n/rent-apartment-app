package com.example.auth_module.unit.web;

import com.example.auth_module.exception.UserException;
import com.example.auth_module.exception.UserExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserExceptionHandlerStandaloneTest {

    private MockMvc mvc;

    // Мини-контроллер просто кидает нужные исключения
    @RestController
    @RequestMapping("/test")
    static class DummyController {

        @GetMapping("/user-401")
        public String user401() {
            throw new UserException(
                UserException.AUTHORIZATION_TOKEN_FAILED,
                UserException.AUTHORIZATION_TOKEN_FAILED_CODE // 401
            );
        }

        @GetMapping("/user-404")
        public String user404() {
            throw new UserException("USER_NOT_FOUND", 404);
        }

        @GetMapping("/runtime")
        public String runtime() {
            throw new RuntimeException("boom");
        }

        // ЯВНО провоцируем 400: бросаем HttpMessageNotReadableException
        @PostMapping("/bad-body")
        public String badBody() {
            throw new HttpMessageNotReadableException("bad body", (HttpInputMessage) null);
        }
    }

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
            .standaloneSetup(new DummyController())
            .setControllerAdvice(new UserExceptionHandler()) // <-- подключаем  advice
            .build();
    }

    @Test
    @DisplayName("UserException 401 -> статус 401 + JSON")
    void userException_401() throws Exception {
        mvc.perform(get("/test/user-401"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error", not(isEmptyOrNullString())))
            .andExpect(jsonPath("$.path").value("/test/user-401"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("UserException 404 -> статус 404 + JSON")
    void userException_404() throws Exception {
        mvc.perform(get("/test/user-404"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error", not(isEmptyOrNullString())))
            .andExpect(jsonPath("$.path").value("/test/user-404"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("RuntimeException -> 500 + JSON")
    void runtime_500() throws Exception {
        mvc.perform(get("/test/runtime"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error", not(isEmptyOrNullString())))
            .andExpect(jsonPath("$.path").value("/test/runtime"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("HttpMessageNotReadableException -> 400 Bad Request + JSON")
    void badBody_400() throws Exception {
        mvc.perform(post("/test/bad-body")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error", not(isEmptyOrNullString())))
            .andExpect(jsonPath("$.path").value("/test/bad-body"))
            .andExpect(jsonPath("$.timestamp").exists());
    }
}
