package com.example.auth_module.exception;

import com.example.auth_module.exception.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;

@Log4j2
@RestControllerAdvice
public class UserExceptionHandler {

    // === Доменные ошибки (используем errorCode как HTTP статус, если валиден 4xx/5xx) ===
    @ExceptionHandler(UserException.class)
    public ResponseEntity<ApiError> onUser(UserException e, HttpServletRequest req) {
        HttpStatus st = safeHttpStatus(e.getErrorCode());
        ApiError body = build(st, e.getMessage(), req.getRequestURI());
        return ResponseEntity.status(st).body(body);
    }

    // === Security: 401 / 403 ===
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> onAuth(AuthenticationException e, HttpServletRequest req) {
        HttpStatus st = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(st).body(build(st, "Authentication required or invalid token", req.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> onAccess(AccessDeniedException e, HttpServletRequest req) {
        HttpStatus st = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(st).body(build(st, "Access denied", req.getRequestURI()));
    }

    // === 400: битый JSON ===
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> onBadBody(HttpMessageNotReadableException e, HttpServletRequest req) {
        HttpStatus st = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(st).body(build(st, "Malformed JSON request body", req.getRequestURI()));
    }

    // === JWT: просрочен → 401 ===
    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public ResponseEntity<ApiError> onExpired(io.jsonwebtoken.ExpiredJwtException e, HttpServletRequest req) {
        HttpStatus st = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(st).body(build(st, "Token expired", req.getRequestURI()));
    }

    // === JWT: общий случай → 401 ===
    @ExceptionHandler(io.jsonwebtoken.JwtException.class)
    public ResponseEntity<ApiError> onJwt(io.jsonwebtoken.JwtException e, HttpServletRequest req) {
        HttpStatus st = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(st).body(build(st, "Invalid token", req.getRequestURI()));
    }

    // === 404: не найдено ===
    @ExceptionHandler({
        jakarta.persistence.EntityNotFoundException.class,
        org.springframework.security.core.userdetails.UsernameNotFoundException.class
    })
    public ResponseEntity<ApiError> onNotFound(RuntimeException e, HttpServletRequest req) {
        HttpStatus st = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(st).body(build(st, "Resource not found", req.getRequestURI()));
    }

    // === 409: конфликт целостности (unique и т.п.) ===
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> onConflict(org.springframework.dao.DataIntegrityViolationException e,
                                               HttpServletRequest req) {
        HttpStatus st = HttpStatus.CONFLICT;
        return ResponseEntity.status(st).body(build(st, "Conflict: integrity constraint", req.getRequestURI()));
    }

    // === 400: неверные параметры ===
    @ExceptionHandler({
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
        org.springframework.web.bind.MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiError> onBadParams(Exception e, HttpServletRequest req) {
        HttpStatus st = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(st).body(build(st, "Bad request parameters", req.getRequestURI()));
    }

    // === 400: валидация DTO ===
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> onValidation(org.springframework.web.bind.MethodArgumentNotValidException e,
                                                 HttpServletRequest req) {
        HttpStatus st = HttpStatus.BAD_REQUEST;
        String msg = e.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + (fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage()))
            .reduce((a, b) -> a + "; " + b).orElse("Validation failed");
        return ResponseEntity.status(st).body(build(st, msg, req.getRequestURI()));
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
        org.springframework.web.server.ResponseStatusException ex,
        jakarta.servlet.http.HttpServletRequest request
    ) {
        var body = new java.util.LinkedHashMap<String, Object>();
        body.put("error", ex.getReason() != null ? ex.getReason() : "Error");
        body.put("status", ex.getStatusCode().value());
        body.put("path", request.getRequestURI());
        body.put("timestamp", java.time.Instant.now().toString());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    // === Остальное → 500 ===
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> onOther(Exception e, HttpServletRequest req) {
        log.error("Unexpected error at {}: {}", req.getRequestURI(), e.getMessage(), e);
        HttpStatus st = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(st).body(build(st, "Unexpected server error", req.getRequestURI()));
    }

    // === Вспомогательные методы ===
    private static ApiError build(HttpStatus status, String message, String path) {
        return ApiError.builder()
            .error(message)
            .status(status.value())
            .path(path)
            .timestamp(Instant.now())
            .build();
    }

    private static HttpStatus safeHttpStatus(int code) {
        if (code >= 400 && code <= 599) {
            try {
                return HttpStatus.valueOf(code);
            } catch (Exception ignored) {  }
        }
        return HttpStatus.BAD_REQUEST;
    }
}
