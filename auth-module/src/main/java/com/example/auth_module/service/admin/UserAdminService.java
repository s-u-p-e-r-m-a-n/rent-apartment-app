package com.example.auth_module.service.admin;

import com.example.auth_module.dto.UserShortDto;
import com.example.auth_module.model.UserEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;


public interface UserAdminService {

    public UserShortDto changeRole(Long targetUserId, UserEntity.Role targetRole,
                                   String actorLogin, Set<String> actorAuthorities);

}
