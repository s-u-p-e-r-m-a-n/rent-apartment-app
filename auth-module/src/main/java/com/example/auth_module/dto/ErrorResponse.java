package com.example.auth_module.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Стандартный формат ошибки")
public record ErrorResponse(
    @Schema(description = "HTTP статус")
    int status,

    @Schema(description = "Сообщение об ошибке")
    String message
) {
}
