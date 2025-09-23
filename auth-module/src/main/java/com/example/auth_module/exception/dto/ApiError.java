package com.example.auth_module.exception.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
@Schema(name = "ApiError", description = "Стандартное тело ошибки от глобального обработчика.")
public class ApiError {
    @Schema(description = "Название статуса HTTP")
    String error;

    @Schema(description = "HTTP статус (число)")
    int status;

    @Schema(description = "Путь запроса")
    String path;

    @Schema(description = "Момент возникновения ошибки (UTC)", format = "date-time",
        example = "2025-09-21T20:15:45Z")
    Instant timestamp;
}
