package com.example.auth_module.unit.web;

import com.example.auth_module.exception.UserException;
import com.example.auth_module.exception.UserExceptionHandler;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
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

        // 400: отсутствует обязательный @RequestParam
        @GetMapping("/missing-param")
        public void missingParam(@RequestParam String q) {
            // не вызывается — ошибка поднимется раньше
        }

        // 400: несоответствие типа @PathVariable (ожидаем Long, придёт "abc")
        @GetMapping("/type-mismatch/{id}")
        public String typeMismatch(@PathVariable Long id) {
            return "ok-" + id;
        }

        // 409: конфликт целостности (дубликат и т.п.)
        @GetMapping("/conflict")
        public void conflict() {
            throw new DataIntegrityViolationException("duplicate");
        }

        // 400: защита safeHttpStatus — странный код UserException (неизвестный статус)
        @GetMapping("/user-weird")
        public void userWeird() {
            throw new UserException("weird", 999);
        }

        @GetMapping("/jwt-expired")
        public void jwtExpired() {
            throw new ExpiredJwtException(null, null, "expired");
        }

        @GetMapping("/auth-error")
        public void authError() {
            throw new AuthenticationException("bad token") {
            };
        }

        @PostMapping("/validate")
        public void validate(@Valid @RequestBody TestDto dto) {
        }

        static class TestDto {
            @jakarta.validation.constraints.Email
            public String email;
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

    @DisplayName("Advice: MissingServletRequestParameterException -> 400 + JSON")
    @Test
    void advice_missingParam_400() throws Exception {
        mvc.perform(get("/test/missing-param"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.path").value("/test/missing-param"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @DisplayName("Advice: MethodArgumentTypeMismatchException -> 400 + JSON")
    @Test
    void advice_typeMismatch_400() throws Exception {
        mvc.perform(get("/test/type-mismatch/abc"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.path").value("/test/type-mismatch/abc"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @DisplayName("Advice: DataIntegrityViolationException -> 409 + JSON")
    @Test
    void advice_dataIntegrity_409() throws Exception {
        mvc.perform(get("/test/conflict"))
            .andExpect(status().isConflict())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.path").value("/test/conflict"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @DisplayName("Advice: UserException(code=999) -> 400 (safeHttpStatus) + JSON")
    @Test
    void advice_userException_weirdCode_mapsTo400() throws Exception {
        mvc.perform(get("/test/user-weird"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.path").value("/test/user-weird"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @DisplayName("Advice: ExpiredJwtException -> 401 + JSON")
    @Test
    void advice_jwtExpired_401() throws Exception {
        mvc.perform(get("/test/jwt-expired"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }

    @DisplayName("Advice: AuthenticationException -> 401 + JSON")
    @Test
    void advice_authError_401() throws Exception {
        mvc.perform(get("/test/auth-error"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }

    @DisplayName("Advice: MethodArgumentNotValidException -> 400 + JSON")
    @Test
    void advice_validation_400() throws Exception {
        mvc.perform(post("/test/validate")
                .contentType("application/json")
                .content("{\"email\":\"bad-email\"}")) // невалидный email
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }


}
