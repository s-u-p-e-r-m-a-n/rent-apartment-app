package com.example.auth_module.service.impl;


import com.example.auth_module.dto.SenderDto;
import com.example.auth_module.service.EmailSenderIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("docker")
public class StubEmailSenderIntegrationService implements EmailSenderIntegrationService {

    private static final Logger log =
        LoggerFactory.getLogger(StubEmailSenderIntegrationService.class);

    @Override
    public String sendCodeVerification(SenderDto dto) {
        log.info(
            "[STUB][docker] Отправка письма пропущена. Будет отправлен код {} на письмо {}",
            dto.getCode(), dto.getEmail()
        );

        return "код отправлен";
    }
}

