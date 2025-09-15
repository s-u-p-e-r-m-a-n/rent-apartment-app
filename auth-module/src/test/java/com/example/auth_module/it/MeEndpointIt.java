package com.example.auth_module.it;

import com.example.auth_module.dto.TokenResponseDto;
import com.example.auth_module.dto.UserRequestDto;
import com.example.auth_module.repository.UserRepository;
import com.example.auth_module.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;

class MeEndpointIt extends BaseIT {

    @Autowired
    AuthService authService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TestRestTemplate rest;

    private final String username = "Serega";
    private final String email = "it_me@example.com";
    private final String pwd = "Password123!";


    @Test
    @DisplayName("GET /api/auth/me: 403 без токена и 200 с Bearer JWT")
    void me_requires_bearer_token() {
        // 1) Без токена → 401 (из-за @PreAuthorize(\"isAuthenticated()\"))
        var noAuth = rest.getForEntity("/api/auth/me", String.class);
        assertThat(noAuth.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // 2) Регистрация + первый логин через /authorization (нужен код)
        authService.registration(new UserRequestDto(username, email, pwd, null));
        String code = userRepository.findByEmailJpql(email).orElseThrow().getVerification();

        var req = new UserRequestDto(username, email, pwd, code);
        var tokenResp = rest.postForEntity("/api/auth/authorization", req, TokenResponseDto.class);
        assertThat(tokenResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String jwt = null;

        if (!isNull(tokenResp.getBody())) {
            jwt = tokenResp.getBody().accessToken();
        }

        // 3) С токеном → 200
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(jwt);
        var ok = rest.exchange("/api/auth/me", HttpMethod.GET, new HttpEntity<>(h), String.class);

        assertThat(ok.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ok.getBody()).isNotBlank();
    }
}
