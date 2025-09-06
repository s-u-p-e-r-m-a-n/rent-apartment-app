package com.example.auth_module.repository;

import com.example.auth_module.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long>, UserRepositoryCriteria{

    @Query(value = "select u from UserEntity u where u.login = :email")
    public Optional<UserEntity> findByEmailJpql(String email);

    @Query(value = "select u from UserEntity u where u.username = :username")
    public  Optional<UserEntity> findUsernameJpql(String username);

    // добавляем вариант с ролями
    @Query("select u from UserEntity u left join fetch u.roles where u.login = :email")
    Optional<UserEntity> fetchWithRolesByEmail(String email);

    @Query(value = "select u from UserEntity u")
    public List<UserEntity> findAllUsersJpql();

   //перед сохранением нового пользователя проверяем, что login ещё не занят
    boolean existsByLogin(String login);
    //и что username тоже уникальный.
    boolean existsByUsername(String username);

}
