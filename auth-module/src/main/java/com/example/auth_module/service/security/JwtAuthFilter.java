package com.example.auth_module.service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;



/**
 * Перехватывает запрос, вытаскивает Bearer JWT, валидирует и
 * кладёт Authentication в SecurityContext так, чтобы работал @PreAuthorize.
 *
 * ВАЖНО:
 * - В  JwtService subject = login.
 * - Роли в токене: ["ADMIN","SUPER_ADMIN","USER","GUEST"].
 * - Здесь мапим их в GrantedAuthority с префиксом ROLE_ (требование Spring).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
        throws ServletException, IOException {

        // 1) Забираем токен из заголовка Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // нет токена — идём дальше без аутентификации
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);

        try {
            // 2) Валидируем подпись/exp
            if (!jwtService.isValid(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 3) Достаём клеймы
            Claims claims = jwtService.parse(token).getPayload();
            String login = claims.getSubject();                 // subject = login
            List<String> roles = jwtService.getRoles(token);   // ["ADMIN", "USER", ...]
            request.setAttribute("jwt_exp", claims.getExpiration()); // java.util.Date

            // 4) Мапим роли в GrantedAuthority (ROLE_*)
            Collection<? extends GrantedAuthority> authorities =
                roles == null ? List.of()
                    : roles.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .collect(Collectors.toList());

            // 5) Создаём Authentication (principal = login)
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(login, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 6) Кладём в контекст
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtException | IllegalArgumentException e) {
            // битый/просроченный/неверный токен — просто идём дальше без аутентификации
            SecurityContextHolder.clearContext();
        }

        // 7) Пропускаем дальше
        filterChain.doFilter(request, response);
    }


}
