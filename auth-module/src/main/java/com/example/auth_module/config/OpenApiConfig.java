package com.example.auth_module.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.info.Contact;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;



import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        Components components = new Components();

        // 1) Общая схема тела ошибки
        Schema<?> apiError = new Schema<>()
            .name("ApiError")
            .type("object")
            .addProperties("error", new StringSchema())
            .addProperties("status", new IntegerSchema())
            .addProperties("path", new StringSchema())
            .addProperties("timestamp", new Schema<>().type("string").format("date-time"));
        components.addSchemas("ApiError", apiError);

        // 2) Security: Bearer JWT
        components.addSecuritySchemes("bearerAuth",
            new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT"));

        // ссылка на схему ApiError
        Schema<?> apiErrorRef = new Schema<>().$ref("#/components/schemas/ApiError");

// хелпер для ответа с example
        BiFunction<String, Map<String, Object>, ApiResponse> make =
            (desc, ex) -> {
                MediaType mt = new MediaType().schema(apiErrorRef).example(ex);
                Content content = new Content().addMediaType("application/json", mt);
                return new ApiResponse().description(desc).content(content);
            };

// единые примеры
        String now = "2025-09-21T20:15:45Z";
        var ex400 = new LinkedHashMap<String, Object>() {{
            put("error", "Bad request parameters");
            put("status", 400);
            put("path", "/api/example");
            put("timestamp", now);
        }};
        var ex401 = new LinkedHashMap<String, Object>() {{
            put("error", "Authentication required or invalid token");
            put("status", 401);
            put("path", "/api/example");
            put("timestamp", now);
        }};
        var ex403 = new LinkedHashMap<String, Object>() {{
            put("error", "Access denied");
            put("status", 403);
            put("path", "/api/example");
            put("timestamp", now);
        }};
        var ex404 = new LinkedHashMap<String, Object>() {{
            put("error", "Resource not found");
            put("status", 404);
            put("path", "/api/example");
            put("timestamp", now);
        }};
        var ex409 = new LinkedHashMap<String, Object>() {{
            put("error", "Conflict: integrity constraint");
            put("status", 409);
            put("path", "/api/example");
            put("timestamp", now);
        }};
        var ex422 = new LinkedHashMap<String, Object>() {{
            put("error", "Validation failed");
            put("status", 422);
            put("path", "/api/example");
            put("timestamp", now);
        }};
        var ex500 = new LinkedHashMap<String, Object>() {{
            put("error", "Unexpected server error");
            put("status", 500);
            put("path", "/api/example");
            put("timestamp", now);
        }};


        components
            .addResponses("BadRequest", make.apply("Bad Request", ex400))
            .addResponses("Unauthorized", make.apply("Unauthorized", ex401))
            .addResponses("Forbidden", make.apply("Forbidden", ex403))
            .addResponses("NotFound", make.apply("Not Found", ex404))
            .addResponses("Conflict", make.apply("Conflict", ex409))
            .addResponses("UnprocessableEntity", make.apply("Unprocessable Entity", ex422))
            .addResponses("ServerError", make.apply("Internal Server Error", ex500));


        Info info = new Info()
            .title("Rent Apartment — Auth API")
            .version("v1")
            .description("""
                Модуль аутентификации: регистрация, авторизация, профиль, админ-операции.
                Единый формат ошибок: ApiError { error, status, path, timestamp }.
                """)
            .contact(new Contact()
                .name("Your Name")
                .email("you@example.com")
                .url("https://github.com/your-handle"))
            .license(new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0"));


        // --- возвращаем итоговый OpenAPI ---
        return new OpenAPI()
            .components(components)
            .info(info);

    }

}
