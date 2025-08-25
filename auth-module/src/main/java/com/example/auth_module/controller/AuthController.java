package com.example.auth_module.controller;

import com.example.auth_module.dto.TokenResponseDto;
import com.example.auth_module.dto.UserRequestDto;
import com.example.auth_module.dto.UserResponseDto;
import com.example.auth_module.exception.UserException;
import com.example.auth_module.mapper.AuthMapper;
import com.example.auth_module.repository.UserRepository;
import com.example.auth_module.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.auth_module.controller.AuthControllerPath.*;
import static com.example.auth_module.exception.UserException.ACCESS_DENIED;

@Log4j2
@RequiredArgsConstructor
@RestController
public class AuthController {
    private final AuthMapper authMapper;
    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping(REGISTRATION_NEW_USER)
    public String registration(@RequestBody UserRequestDto userRequestDto) {
        return authService.registration(userRequestDto);
    }

    @PostMapping(ADMIN_USER)
    public List<UserResponseDto> admin(@RequestBody UserRequestDto userRequestDto) {
        String role = authService.checkingUserRole(userRequestDto);
        if (role.equals("[ADMIN]") || role.equals("[SUPER_ADMIN]")) {
            return authMapper.mappUserEntityToUserResponseDto(userRepository.findAllUsersJpql());
        } else throw new UserException(ACCESS_DENIED, 702);
    }

    @GetMapping(CHANGE_ROLE_USERS)
    public String changeUserRole(@PathVariable Long id) {

        return authService.changeRole(id);

    }
    @PostMapping(DELETE_USER)
    public List<UserResponseDto> delete(@PathVariable Long id) {
       return authService.deleteUser(id);
    }

    @PostMapping(AUTHORIZATION_USER)
    public TokenResponseDto authorization(@RequestBody UserRequestDto userRequestDto) {
        return authService.authorization(userRequestDto);
    }

    @GetMapping(ADD_COMMENT)
    public String addComment(@RequestParam String comment, @RequestHeader String token) {

        return null;
    }
    @GetMapping("/api/auth/")
    public String test(){
        return "test";
    }
    @PostMapping("/api/auth/")
    public String test1(){
        return "test1";
            }
}
