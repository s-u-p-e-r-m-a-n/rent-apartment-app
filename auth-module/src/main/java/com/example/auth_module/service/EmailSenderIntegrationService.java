package com.example.auth_module.service;

import com.example.auth_module.dto.SenderDto;

public interface EmailSenderIntegrationService {

    public String sendCodeVerification(SenderDto senderDto);
}
