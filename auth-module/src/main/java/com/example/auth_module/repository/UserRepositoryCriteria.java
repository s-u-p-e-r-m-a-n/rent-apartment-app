package com.example.auth_module.repository;

import com.example.auth_module.model.UserEntity;

import java.util.Optional;


public interface UserRepositoryCriteria {


     public Optional<UserEntity> findByLoginCriteria(String login);

}
