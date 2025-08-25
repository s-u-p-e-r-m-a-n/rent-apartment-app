package com.example.auth_module.mapper;

import com.example.auth_module.dto.UserRequestDto;
import com.example.auth_module.dto.UserResponseDto;
import com.example.auth_module.model.UserEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface AuthMapper {

    @Mapping(target = "login", source = "loginValue")
    @Mapping(target = "username", source = "usernameValue")
    public UserEntity mappUserDtoToUserEntity(UserRequestDto dto);

    @AfterMapping
    public default void afterMappUserDtoToUserEntity(@MappingTarget UserEntity user, UserRequestDto dto) {
        user.setDateRegistration(LocalDateTime.now());
        user.setRoles(Set.of(UserEntity.Role.GUEST));
    }

    public List<UserResponseDto> mappUserEntityToUserResponseDto(List<UserEntity> user);

    @AfterMapping
    public default void afterMappUserEntityToUserResponseDto(@MappingTarget UserResponseDto dto, UserEntity user) {
        dto.setRole(user.getRoles().toString());
    }

}
