package com.example.auth_module.it;

import com.example.auth_module.dto.UserRequestDto;
import com.example.auth_module.repository.UserRepository;
import com.example.auth_module.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class AuthAuthorizationNegativeIt extends BaseIT {

    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;
    @Autowired TestRestTemplate rest;

    private static final String AUTHZ_URL = "/api/auth/authorization";
    @Test
    @DisplayName("authorization: верный email + неверный пароль → 401 UNAUTHORIZED")
    void authorization_wrong_password_401() {
        // arrange
        var username = "Serega";
        var email = "it_authz_wrong_pwd@example.com";
        var goodPwd = "Password123!";
        authService.registration(new UserRequestDto(username, email, goodPwd, null));
        var code = userRepository.findByEmailJpql(email).orElseThrow().getVerification();

        // act
        var badReq = new UserRequestDto(username, email, "WrongPass123!", code);
        ResponseEntity<String> resp = rest.postForEntity(AUTHZ_URL, badReq, String.class);

        // assert
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    }

    @Test
    @DisplayName("authorization: верный email/пароль + неверный код → 403 FORBIDDEN")
    void authorization_wrong_code_403() {
        // arrange
        var username = "Serega";
        var email = "it_authz_wrong_code@example.com";
        var pwd = "Password123!";
        authService.registration(new UserRequestDto(username, email, pwd, null));
        userRepository.findByEmailJpql(email).orElseThrow();

        // act
        var wrongCodeReq = new UserRequestDto(username, email, pwd, "0000");
        ResponseEntity<String> resp = rest.postForEntity(AUTHZ_URL, wrongCodeReq, String.class);

        // assert
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }
}
