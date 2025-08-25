package com.example.auth_module.repository;

import com.example.auth_module.model.UserEntity;
import com.example.auth_module.repository.Impl.UserRepositoryCriteriaImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для UserRepository (repository)")
public class UserRepositoryUnitTests {
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private UserRepositoryCriteriaImpl userRepositoryCriteria;

    @Test
    @DisplayName("findByLoginCriteria возвращает Optional с пользователем")
    void testFindByLoginCriteria_UserExists() {
        // Подготовка
        UserEntity userEntity = new UserEntity();
        userEntity.setLogin("test@mail");

        // Моки для Criteria API
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        CriteriaQuery<UserEntity> criteriaQuery = mock(CriteriaQuery.class);
        Root<UserEntity> root = mock(Root.class);
        TypedQuery<UserEntity> typedQuery = mock(TypedQuery.class);
        Predicate predicate = mock(Predicate.class);

        // Мокаем вызовы entityManager
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(UserEntity.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(UserEntity.class)).thenReturn(root);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(criteriaBuilder.equal(root.get("login"), "test@mail")).thenReturn(predicate);
        when(criteriaQuery.where(predicate)).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultStream()).thenReturn(Stream.of(userEntity));

        // Вызов метода
        Optional<UserEntity> result = userRepositoryCriteria.findByLoginCriteria("test@mail");

        // Проверки
        assertTrue(result.isPresent());
        assertEquals("test@mail", result.get().getLogin());

        // Проверяем взаимодействия с моками
        verify(entityManager).getCriteriaBuilder();
        verify(entityManager).createQuery(criteriaQuery);
        verify(typedQuery).getResultStream();
    }

    @Test
    @DisplayName("findByLoginCriteria возвращает пустой Optional, если пользователя нет")
    void testFindByLoginCriteria_UserNotFound() {
        // Моки
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        CriteriaQuery<UserEntity> criteriaQuery = mock(CriteriaQuery.class);
        Root<UserEntity> root = mock(Root.class);
        TypedQuery<UserEntity> typedQuery = mock(TypedQuery.class);
        Predicate predicate = mock(Predicate.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(UserEntity.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(UserEntity.class)).thenReturn(root);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(criteriaBuilder.equal(root.get("login"), "test@mail")).thenReturn(predicate);
        when(criteriaQuery.where(predicate)).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultStream()).thenReturn(Stream.empty());

        Optional<UserEntity> result = userRepositoryCriteria.findByLoginCriteria("test@mail");

        assertTrue(result.isEmpty());
    }

}
