package com.example.auth_module.service.impl;

import com.example.auth_module.dto.SenderDto;
import com.example.auth_module.dto.TokenResponseDto;
import com.example.auth_module.dto.UserRequestDto;
import com.example.auth_module.dto.UserResponseDto;
import com.example.auth_module.exception.UserException;
import com.example.auth_module.mapper.AuthMapper;
import com.example.auth_module.model.UserEntity;
import com.example.auth_module.service.RefreshTokenService;
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
import java.util.*;

import static com.example.auth_module.exception.UserException.*;

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
    private final RefreshTokenService refreshTokenService;


    @Override
    @Transactional
    public String registration(UserRequestDto userRequestDto) throws UserException {

        String loginValidate =  validService.validation(userRequestDto.loginValue().toLowerCase(Locale.ROOT));

        //   String loginValidate = userRequestDto.loginValue().toLowerCase(Locale.ROOT);
        userRepository.findByEmailJpql(loginValidate)
            .ifPresent(u -> {
                throw new UserException(REGISTRATION_WITH_LOGIN_FAILED, REGISTRATION_WITH_LOGIN_FAILED_CODE);
            });

        UserEntity userEntity = authMapper.mappUserDtoToUserEntity(userRequestDto);
        userEntity.setLogin(loginValidate);
        if (userEntity.getRoles() == null || userEntity.getRoles().isEmpty()) {
            userEntity.setRoles(new HashSet<>(Set.of(UserEntity.Role.GUEST)));
        }
        //сетим код подтверждения email
        String code = emailCodeGeneration();
        userEntity.setPasswordHash(passwordEncoder.encode(userRequestDto.passwordValue()));
        userEntity.setVerification(code);
        userRepository.save(userEntity);

        //отправка кода верификации
        String resultEmailSend;
        try {
            resultEmailSend = emailSenderIntegrationService.sendCodeVerification(new SenderDto(loginValidate, code));
        } catch (RuntimeException e) {
            throw new UserException(ERROR_SEND_MASSAGE, ERROR_SEND_MASSAGE_CODE);
        }
        //  resultEmailSend = emailSenderIntegrationService.sendCodeVerification(new SenderDto(loginValidate, code));
        return "код отправлен";
    }

    //метод генерирует код для отправки на почту
    private String emailCodeGeneration() {
        Random random = new Random();
        int code = 1000 + random.nextInt(9000);
        return String.valueOf(code);
    }

    @Transactional
    public TokenResponseDto authorization(UserRequestDto userRequestDto) {


        String loginValidate = validService.validation(userRequestDto.loginValue().toLowerCase(Locale.ROOT));
        UserEntity userEntityByLogin = userRepository.findByLoginCriteria(loginValidate)
            .orElseThrow(() -> new UserException(USER_DOES_NOT_EXIST, USER_DOES_NOT_EXIST_CODE));
        if (!passwordEncoder.matches(userRequestDto.passwordValue(), userEntityByLogin.getPasswordHash())) {
            throw new UserException(WRONG_PASSWORD, WRONG_PASSWORD_CODE);
        }

        // если уже верифицирован
        if (VERIFICATION_EQUALS.equals(userEntityByLogin.getVerification())) {
            // access
            String accessToken = jwtService.generateToken(
                userEntityByLogin.getLogin(),
                userEntityByLogin.getRoles()
            );
            long accessExp = jwtService.getExpiryEpochMillis(accessToken);


            var refresh = refreshTokenService.issue(userEntityByLogin);


            return new TokenResponseDto(
                accessToken,
                Instant.ofEpochMilli(accessExp),
                refresh.getToken()
            );
        }


        // ещё не верифицирован
        if (userRequestDto.code() == null) {
            throw new UserException("Verification code is required", 422);
        }

        if (!Objects.equals(userRequestDto.code(), userEntityByLogin.getVerification())) {
            throw new UserException(UNCONFIRMED_EMAIL, UNCONFIRMED_EMAIL_CODE);
        }

        // подтверждаем e-mail
        Set<UserEntity.Role> roles = new HashSet<>(Optional.ofNullable(userEntityByLogin.getRoles())
            .orElseGet(HashSet::new));
        if (roles.contains(UserEntity.Role.GUEST)) {
            roles.remove(UserEntity.Role.GUEST);
            roles.add(UserEntity.Role.USER);
        }
        userEntityByLogin.setRoles(roles);
        userEntityByLogin.setVerification(VERIFICATION_EQUALS);
        userRepository.save(userEntityByLogin);

        // 5) выдаём токен (не сохраняем его в БД)

        String accessToken = jwtService.generateToken(userEntityByLogin.getLogin(), roles);
        long accessExp = jwtService.getExpiryEpochMillis(accessToken);


        var refresh = refreshTokenService.issue(userEntityByLogin);

        return new TokenResponseDto(
            accessToken,
            Instant.ofEpochMilli(accessExp),
            refresh.getToken()
        );
    }


    public List<UserResponseDto> deleteUser(Long id) {
        userRepository.deleteById(id);
        return authMapper.mappUserEntityToUserResponseDto(userRepository.findAllUsersJpql());

    }

}
