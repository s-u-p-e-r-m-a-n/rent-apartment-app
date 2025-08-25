package com.example.auth_module.service.impl;

import com.example.auth_module.dto.SenderDto;
import com.example.auth_module.dto.TokenResponseDto;
import com.example.auth_module.dto.UserRequestDto;
import com.example.auth_module.dto.UserResponseDto;
import com.example.auth_module.exception.UserException;
import com.example.auth_module.mapper.AuthMapper;
import com.example.auth_module.model.UserEntity;
import com.example.auth_module.repository.UserRepository;
import com.example.auth_module.service.AuthService;
import com.example.auth_module.service.EmailSenderIntegrationService;
import com.example.auth_module.service.ValidService;
import com.example.auth_module.service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.auth_module.exception.UserException.*;
import static com.example.auth_module.service.Base64EncodDecod.decode;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final EmailSenderIntegrationService emailSenderIntegrationService;
    private final AuthMapper authMapper;
    private final UserRepository userRepository;
    private final ValidService validService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    public static final String VERIFICATION_EQUALS = "verified";


    @Override
    @Transactional
    public String registration(UserRequestDto userRequestDto) throws UserException {
        try {
            validService.validation(userRequestDto.getLoginValue());
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        String loginValidate = userRequestDto.getLoginValue();
        userRepository.findByEmailJpql(loginValidate)
                .ifPresent(u -> {
                    throw new UserException(REGISTRATION_WITH_LOGIN_FAILED, REGISTRATION_WITH_LOGIN_FAILED_CODE);
                });
        UserEntity userEntity = authMapper.mappUserDtoToUserEntity(userRequestDto);
        //сетим код подтверждения email
        String code = emailCodeGeneration();
        userEntity.setPasswordHash(passwordEncoder.encode(userRequestDto.getPasswordValue()));
        userEntity.setVerification(code);
        userRepository.save(userEntity);
        //отправка кода верификации
        String resultEmailSend;
        try {
            resultEmailSend = emailSenderIntegrationService.sendCodeVerification(new SenderDto(loginValidate, code));
        } catch (RuntimeException e) {
            throw new UserException("ошибка код не отправлен", 666);
        }
        return resultEmailSend;
    }

    //метод генерирует код для отправки на почту
    private String emailCodeGeneration() {
        Random random = new Random();
        int code = 1000 + random.nextInt(9000);
        return String.valueOf(code);
    }

    public String changeRole(Long id) {

        UserEntity userEntity = userRepository.findById(id).orElse(null);
        if (userEntity == null) {
            return null;
        }
        if (userEntity.getRoles().contains(UserEntity.Role.ADMIN)) {
            userEntity.setRoles(new HashSet<>(Set.of(UserEntity.Role.USER)));
            String token = changeTokenForUser(userEntity.getToken());
            userEntity.setToken(token);
            userRepository.save(userEntity);
            return "Роль изменена " + userEntity.getUsername() + " на " + userEntity.getRoles().toString();
        }
        if (userEntity.getRoles().contains(UserEntity.Role.USER)) {
            userEntity.setRoles(new HashSet<>(Set.of(UserEntity.Role.ADMIN)));
            String token = changeTokenForAdmin(userEntity.getToken());
            userEntity.setToken(token);
            userRepository.save(userEntity);


            return "Роль изменена " + userEntity.getUsername() + " на " + userEntity.getRoles().toString();
        }
        return "нет такого пользователя";
    }

    @Transactional
    public TokenResponseDto authorization(UserRequestDto userRequestDto) {
        try {
            validService.validation(userRequestDto.getLoginValue());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }
        String loginValidate = userRequestDto.getLoginValue();
        UserEntity userEntityByLogin = userRepository.findByLoginCriteria(loginValidate)
                .orElseThrow(() -> new UserException(USER_DOES_NOT_EXIST, USER_DOES_NOT_EXIST_CODE));
        //проверка логин и пароль
        if((!passwordEncoder.matches(userRequestDto.getPasswordValue(), userEntityByLogin.getPassword()))){
            if (userRequestDto.getCode().equals(userEntityByLogin.getVerification())) {
                //проверка если код совпадает меняем роль GUEST на USER
                if (userEntityByLogin.getRoles().contains(UserEntity.Role.GUEST)) {
                    userEntityByLogin.getRoles().remove(UserEntity.Role.GUEST);
                    userEntityByLogin.getRoles().add(UserEntity.Role.USER);
                    userEntityByLogin.setVerification(VERIFICATION_EQUALS);
                }
                // userEntityByLogin.setRoles(new HashSet<>(Set.of(UserEntity.Role.USER)));

            } else if (userEntityByLogin.getVerification().equals(VERIFICATION_EQUALS)) {
                //String token = generateSessionTokenForAuth(userEntityByLogin);
                String accessToken = jwtService.generateToken(userEntityByLogin.getLogin(), userEntityByLogin.getRoles());
                long accessExp = jwtService.getExpiryEpochMillis(accessToken);
                // userEntityByLogin.setToken(token);
                userRepository.save(userEntityByLogin);
                //return "Здравствуйте " + userEntityByLogin.getUsername() + " вы успешно авторизировались! ";
                return new TokenResponseDto(accessToken, Instant.ofEpochMilli(accessExp));
            } else  throw new UserException(UNCONFIRMED_EMAIL,405);
        }
         throw new UserException(WRONG_PASSWORD,404);
    }

    public String checkingUserRole(UserRequestDto userRequestDto) throws UserException {

        String loginValidate = null;
        try {
            validService.validation(userRequestDto.getLoginValue());
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        UserEntity loginCriteria = userRepository.findByLoginCriteria(loginValidate)
                .orElseThrow(() -> new UserException(USER_DOES_NOT_EXIST, USER_DOES_NOT_EXIST_CODE));
        if (loginCriteria.getToken() == null) {
            throw new UserException(AUTHORIZATION_TOKEN_FAILED, AUTHORIZATION_TOKEN_FAILED_CODE);
        }

        String resultRole = gettingRole(loginCriteria.getToken());

        return resultRole;
    }

    private String gettingRole(String token) {

        String[] split = token.split("\\|");

        return split[1];

    }

    private String generateSessionTokenForAuth(UserEntity userEntity) {
        String uniqueToken = UUID.randomUUID().toString();
        return uniqueToken + "|" + userEntity.getRoles() + "|" + LocalDateTime.now().plusDays(1L);
    }


    private String changeTokenForAdmin(String token) {
// Находим индекс первой черты
        int firstPipeIndex = token.indexOf('|');

        if (firstPipeIndex != -1) {
            // Находим индекс второй черты
            int secondPipeIndex = token.indexOf('|', firstPipeIndex + 1);

            if (secondPipeIndex != -1) {
                // Разделяем строку на три части: до первой черты, между чертами и после второй черты
                String beforeFirstPipe = token.substring(0, firstPipeIndex + 1); // "5b186d98-477d-47c0-b822-7ac3f3b5fb7e|"
                String betweenPipes = token.substring(firstPipeIndex + 1, secondPipeIndex); // "[USER]"
                String afterSecondPipe = token.substring(secondPipeIndex); // "|2025-03-23T10:28:30.098937100"

                // Заменяем [USER] на [ADMIN]
                String replaced = betweenPipes.replace(UserEntity.Role.USER.toString(), UserEntity.Role.ADMIN.toString());

                // Собираем строку обратно
                String newToken = beforeFirstPipe + replaced + afterSecondPipe;
                return newToken;
            }

        }
        return null;
    }

    public List<UserResponseDto> deleteUser(Long id) {
        userRepository.deleteById(id);
        return authMapper.mappUserEntityToUserResponseDto(userRepository.findAllUsersJpql());

    }

    private String changeTokenForUser(String token) {

        // Находим индекс первой черты
        int firstPipeIndex = token.indexOf('|');

        if (firstPipeIndex != -1) {
            // Находим индекс второй черты
            int secondPipeIndex = token.indexOf('|', firstPipeIndex + 1);

            if (secondPipeIndex != -1) {
                // Разделяем строку на три части: до первой черты, между чертами и после второй черты
                String beforeFirstPipe = token.substring(0, firstPipeIndex + 1); // "5b186d98-477d-47c0-b822-7ac3f3b5fb7e|"
                String betweenPipes = token.substring(firstPipeIndex + 1, secondPipeIndex); // "[USER]"
                String afterSecondPipe = token.substring(secondPipeIndex); // "|2025-03-23T10:28:30.098937100"

                // Заменяем [ADMIN] на [USER]
                String replaced = betweenPipes.replace(UserEntity.Role.ADMIN.toString(), UserEntity.Role.USER.toString());

                // Собираем строку обратно
                String newToken = beforeFirstPipe + replaced + afterSecondPipe;
                return newToken;
            }

        }
        return null;
    }
}