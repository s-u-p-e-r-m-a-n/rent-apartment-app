package com.example.auth_module.sheduller;

import com.example.auth_module.model.UserEntity;
import com.example.auth_module.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

//@EnableScheduling
//@Component
//@Slf4j
//@RequiredArgsConstructor
public class TokenScheduler {

//    private final UserRepository userRepository;
//
//    @Scheduled(initialDelay=5000,fixedRate = 120000)
//    private void checkToken() {
//        log.info("Планировщик начал работу");
//        List<UserEntity> users = userRepository.findUserEntitiesByTokenIsNullJpql();
//        log.info(users.size() + " активные сессии");
//        for (UserEntity user : users) {
//            String token = user.getToken();
//            String[] split = token.split("\\|");
//            LocalDateTime date = LocalDateTime.parse(split[2]);
//            if (date.isBefore(LocalDateTime.now())) {
//                user.setToken(null);
//                userRepository.save(user);
//                log.info("пользователь " + user.getUsername() + " сессия закрыта");
//            }
//        }
//    }
}
