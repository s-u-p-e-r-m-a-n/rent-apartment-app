package com.example.auth_module.repository.Impl;

import com.example.auth_module.model.UserEntity;
import com.example.auth_module.repository.UserRepositoryCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.Optional;

public class UserRepositoryCriteriaImpl implements UserRepositoryCriteria {

    private final EntityManager entityManager;

    public UserRepositoryCriteriaImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

   public Optional<UserEntity> findByLoginCriteria(String loginValidate){
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder(); //Создаем вспомогательные объекты для критериа запросов
            CriteriaQuery<UserEntity> query = criteriaBuilder.createQuery(UserEntity.class);//специальный тип запроса с указанием типа (дженериком) объекта
            Root<UserEntity> root = query.from(UserEntity.class);// создаем запрос для сущности и сохраняем в root

            query.select(root).where(criteriaBuilder.equal(root.get("login"), loginValidate));//формируем полный критериа запрос к бд с условием
       //UserEntity singleResult = entityManager.createQuery(query).getResultStream().findFirst().orElse(null);
            return entityManager.createQuery(query)
                    .getResultStream()
                    .findFirst();
        };

}
