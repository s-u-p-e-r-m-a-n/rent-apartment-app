package com.example.auth_module.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.time.Instant;

@Log4j2
@RestControllerAdvice
public class UserExceptionHandler {

    // === (используем errorCode как HTTP статус, если это 4xx/5xx) ===
    @ExceptionHandler(UserException.class)
    public ResponseEntity<ApiError> onUser(UserException e, HttpServletRequest req) {
        HttpStatus status = mapStatus(e.getErrorCode());
        log.warn("UserException {} {} at {}",
            e.getErrorCode(), e.getMessage(), req.getRequestURI());
        return ResponseEntity.status(status)
            .body(ApiError.of(e.getMessage(), e.getErrorCode(), status, req.getRequestURI()));
    }

    // === Security: 401 / 403 ===
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> onAuth(AuthenticationException e, HttpServletRequest req) {
        HttpStatus st = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(st)
            .body(ApiError.of("Authentication required or invalid token", st.value(), st, req.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> onAccess(AccessDeniedException e, HttpServletRequest req) {
        HttpStatus st = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(st)
            .body(ApiError.of("Access denied", st.value(), st, req.getRequestURI()));
    }

    // === Не читаемое тело запроса (битый JSON) → 400 ===
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> onBadBody(HttpMessageNotReadableException e, HttpServletRequest req) {
        HttpStatus st = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(st)
            .body(ApiError.of("Malformed JSON request body", st.value(), st, req.getRequestURI()));
    }

    // === Остальное → 500 ===
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> onOther(Exception e, HttpServletRequest req) {
        log.error("Unexpected error at {}: {}", req.getRequestURI(), e.getMessage(), e);
        HttpStatus st = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(st)
            .body(ApiError.of("Unexpected server error", st.value(), st, req.getRequestURI()));
    }

    // === Вспомогалки ===
    private HttpStatus mapStatus(int code) {
        if (code >= 400 && code <= 599) {
            try { return HttpStatus.valueOf(code); }
            catch (Exception ignore) { /* fall through */ }
        }
        return HttpStatus.BAD_REQUEST; // дефолт
    }
    //ловим ошибки на DTO
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> onValidation(org.springframework.web.bind.MethodArgumentNotValidException e,
                                                 HttpServletRequest req) {
        HttpStatus st = HttpStatus.BAD_REQUEST;
        String msg = e.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + (fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage()))
            .reduce((a, b) -> a + "; " + b).orElse("Validation failed");
        return ResponseEntity.status(st)
            .body(ApiError.of(msg, st.value(), st, req.getRequestURI()));
    }


    @Value
    @Builder
    static class ApiError {
        String error;
        int status;       // http статус как число
        String path;      // запрос
        Instant timestamp;// время

        static ApiError of(String error, int statusCode, HttpStatus st, String path) {
            return ApiError.builder()
                .error(error)
                .status(statusCode != 0 ? statusCode : st.value())
                .path(path)
                .timestamp(Instant.now())
                .build();
        }
    }
}

