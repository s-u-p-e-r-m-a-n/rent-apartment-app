package com.example.auth_module.service.security;

import com.example.auth_module.model.UserEntity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component
public class JwtService {
    //берем из properties
    @Value("${jwt.secret}")
    private String secret;            // >= 32 символов
    //берем из properties
    @Value("${jwt.ttl-min}")
    private long ttlMin;              // срок жизни токена (мин)


    //Из строки secret получаем HMAC-ключ (SecretKey) для HS256/HS512.
//Если секрет слишком короткий, JJWT кинет исключение — это защита от слабых ключей.
    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Выпуск JWT: subject=login, roles, iat/exp
     */
    public String generateToken(String login, Collection<Role> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(login)                               // «кто» — обычно login/email
            .claim("roles", roles.stream().map(Enum::name).toList()) // свои клеймы
            .issuedAt(Date.from(now))                     // iat — когда выпущен
            .expiration(Date.from(now.plus(ttlMin, ChronoUnit.MINUTES))) // exp — срок issuedAt/expiration — библиотека потом проверит срок действия автоматически.
            .signWith(key())                              // HMAC-подпись секретом/алгоритм выбирается по типу ключа (HMAC SHA-256).
            .compact();                                   // собрать в строку JWT
    }

    /**
     * Проверка токена
     */
    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Дата истечения в millis
     */
    public long getExpiryEpochMillis(String token) {
        return parse(token).getPayload().getExpiration().toInstant().toEpochMilli();
    }

    /**
     * Валидация/разбор токена (0.12.x API)
     */
    public Jws<Claims> parse(String token) throws JwtException {
        return Jwts.parser()
            .verifyWith(key())           // проверяем подпись нашим ключом
            .build()
            .parseSignedClaims(token);   // парсим и валидируем exp/подпись/формат
//        Если подпись неверна, формат битый или токен просрочен, будет JwtException
//                (в т.ч. ExpiredJwtException).
//                Возвращаем Jws<Claims> — это «подписанные клеймы» (payload + заголовок).
    }

    public String getLogin(String token) {
        return parse(token).getPayload().getSubject(); // 0.12.x: getPayload()
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        return (List<String>) parse(token).getPayload().get("roles");
    }


}
