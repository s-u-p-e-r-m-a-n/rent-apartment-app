package com.example.auth_module.service.impl;

import com.example.auth_module.dto.SenderDto;
import com.example.auth_module.service.EmailSenderIntegrationService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailSenderIntegrationServiceImpl implements EmailSenderIntegrationService {

    private final RestTemplate restTemplate;
    public EmailSenderIntegrationServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String sendCodeVerification(SenderDto senderDto) {
       return  restTemplate.postForObject("http://localhost:9595/sender", senderDto, String.class);
    }
}
