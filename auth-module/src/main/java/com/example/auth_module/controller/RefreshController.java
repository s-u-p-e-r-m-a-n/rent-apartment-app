package com.example.auth_module.controller;


import com.example.auth_module.dto.RefreshRequest;
import com.example.auth_module.dto.TokenResponseDto;
import com.example.auth_module.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RefreshController {

    private final RefreshTokenService refreshTokenService;

    @Operation(
        summary = "Обновление access-токена",
        description = "Принимает refresh-токен, проверяет его и возвращает новую пару токенов (access + refresh)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Успешное обновление",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TokenResponseDto.class),
                examples = @ExampleObject(
                    name = "success",
                    value = """
                        {
                          "accessToken": "eyJhbGciOiJIUzI1NiIs...",
                          "accessTokenExpiresAt": "2025-09-30T12:00:00Z",
                          "refreshToken": "6f5e8e0a-3d0a-4cda-b02d-9b1e3c6d89f4"
                        }
                        """
                ))),
        @ApiResponse(responseCode = "401", description = "Недействительный или просроченный refresh",
            content = @Content(mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ApiError"),
                examples = @ExampleObject(
                    name = "unauthorized",
                    value = """
                        {
                          "error": "Invalid or expired refresh token",
                          "status": 401,
                          "path": "/api/auth/refresh",
                          "timestamp": "2025-09-27T20:15:45Z"
                        }
                        """
                ))),
        @ApiResponse(responseCode = "422",description = "Невалидное тело запроса",
            content = @Content(mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ApiError"),
                examples = @ExampleObject(
                    name = "validation-failed",
                    value = """
                        { "error": "Validation failed", "status": 422,
                          "path": "/api/auth/refresh", "timestamp": "2025-09-21T20:15:45Z" }
                        """
                ))),
        @ApiResponse(responseCode = "500", ref = "#/components/responses/ServerError")
    })
    @PostMapping("/refresh")
    public TokenResponseDto refresh(@io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Тело запроса с refresh-токеном",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = RefreshRequest.class),
            examples = @ExampleObject(
                name = "request",
                value = """
                { "refreshToken": "1f2e4c1b-7d93-44de-9d9a-7af32c85d43e" }
                """
            )
        ))@RequestBody RefreshRequest request) {
        return refreshTokenService.rotate(request.refreshToken());
    }
}
