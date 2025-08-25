package com.example.rent_module.repository;


import com.example.rent_module.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM user_info WHERE login = :email")
    public UserEntity UserEntityByEmail(String email);

    @Query(nativeQuery = true, value = "SELECT  * FROM user_info WHERE username = :username")
    public UserEntity findUserEntityByUsername(String username);

    @Query(nativeQuery = true, value = "SELECT  * FROM user_info WHERE password = :password")
    public UserEntity findUserEntityByPassword(String password);

    @Query(value = "select u from UserEntity u where u.login = :email")
    public UserEntity findByEmailJpql(String email);

    @Query(value = "select u from UserEntity u where u.username = :username")
    public UserEntity findUsernameJpql(String username);

    @Query(value = "select u from UserEntity u where u.password = :password")
    public UserEntity findByPasswordJpql(String password);
    @Query(value = "select u from UserEntity u")
    public List<UserEntity> findAllUsersJpql();

    public List<UserEntity> findUserEntitiesByTokenIsNotNull();
    @Query(value = "select u from UserEntity u  where u.token IS NOT NULL")
    public List<UserEntity> findUserEntitiesByTokenIsNullJpql();
}
