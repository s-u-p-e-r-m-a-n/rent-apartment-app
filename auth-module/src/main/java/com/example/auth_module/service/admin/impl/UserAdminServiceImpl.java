package com.example.auth_module.service.admin.impl;

import com.example.auth_module.dto.UserShortDto;
import com.example.auth_module.model.UserEntity;
import com.example.auth_module.repository.UserRepository;
import com.example.auth_module.service.admin.UserAdminService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 * Бизнес-логика смены роли пользователя.
 * - Правила:
 *   • Сам себе роль менять нельзя.
 *   • SUPER_ADMIN никто не трогает.
 *   • ADMIN может ТОЛЬКО повышать USER->ADMIN.
 *   • Понижать ADMIN->USER может только SUPER_ADMIN.
 * - Никаких самодельных токенов и подсчётов в БД.
 */
@Service
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepository;

    public UserAdminServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * @param targetUserId        ID пользователя, у которого меняем роль (из URL)
     * @param targetRole          Новая роль (USER или ADMIN) — уже провалидирована в контроллере
     * @param actorLogin          Логин того, кто делает запрос (из JWT subject)
     * @param actorAuthorities    Набор прав вида ROLE_ADMIN / ROLE_SUPER_ADMIN (из SecurityContext)
     */
    @Transactional
    public UserShortDto changeRole(Long targetUserId,
                                   UserEntity.Role targetRole,
                                   String actorLogin,
                                   Set<String> actorAuthorities) {

        // --- 1) Загружаем целевого пользователя ---
        UserEntity target = userRepository.findById(targetUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        // --- 2) Защита от изменения себя самого ---
        if (Objects.equals(actorLogin, target.getLogin())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нельзя менять собственную роль");
        }

        // --- 3) SUPER_ADMIN неприкасаем ---
        if (hasSingleRole(target, UserEntity.Role.SUPER_ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нельзя изменять роль SUPER_ADMIN");
        }

        // --- 4) Определяем кто актор: ADMIN или SUPER_ADMIN ---
        boolean actorIsSuperAdmin = actorAuthorities.contains("ROLE_SUPER_ADMIN");
        boolean actorIsAdmin      = actorAuthorities.contains("ROLE_ADMIN");

        if (!actorIsSuperAdmin && !actorIsAdmin) {
            // сюда не должны попасть из-за @PreAuthorize, но дубль-проверка на всякий
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав");
        }

        // --- 5) Текущая роль цели (ожидаем ровно одну активную роль после проверок) ---
        UserEntity.Role current = firstOr(target.getRoles(), UserEntity.Role.GUEST);

        // --- 6) Правила:
        // ADMIN:
        //   • может ТОЛЬКО повышать USER -> ADMIN
        //   • НЕ может понижать ADMIN -> USER
        // SUPER_ADMIN:
        //   • может повышать и понижать USER <-> ADMIN
        if (actorIsAdmin && !actorIsSuperAdmin) {
            // Актор — обычный ADMIN
            if (current == UserEntity.Role.USER && targetRole == UserEntity.Role.ADMIN) {
                // Разрешено: повышение
                setSingleRole(target, UserEntity.Role.ADMIN);
            } else {
                // Любое иное действие админу запрещено (включая понижение админа)
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ADMIN может только повышать USER -> ADMIN");
            }
        } else {
            // Актор — SUPER_ADMIN
            if (targetRole == UserEntity.Role.USER || targetRole == UserEntity.Role.ADMIN) {
                setSingleRole(target, targetRole);
            } else {
                // На всякий случай (хотя контроллер уже отфильтровал)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Разрешено только USER или ADMIN");
            }
        }

        // --- 7) Сохраняем и отдаём короткий ответ ---
        userRepository.save(target);
        return UserShortDto.from(target);
    }

    /** Проверка: сущность имеет РОВНО одну роль и она равна expected. */
    private boolean hasSingleRole(UserEntity u, UserEntity.Role expected) {
        return u.getRoles() != null
            && u.getRoles().size() == 1
            && u.getRoles().contains(expected);
    }

    /** Берём первую роль из множества или возвращаем запасную. */
    private UserEntity.Role firstOr(Set<UserEntity.Role> roles, UserEntity.Role fallback) {
        if (roles == null || roles.isEmpty()) return fallback;
        return roles.iterator().next();
    }

    /** Жёстко выставляем РОВНО одну роль (наша политика одной активной роли). */
    private void setSingleRole(UserEntity u, UserEntity.Role role) {
        Set<UserEntity.Role> one = new HashSet<>();
        one.add(role);
        u.setRoles(one);
    }
}
