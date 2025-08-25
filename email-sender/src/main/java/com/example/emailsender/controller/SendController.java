package com.example.emailsender.controller;

import com.example.emailsender.dto.SenderDto;
import com.example.emailsender.service.impl.SendEmailServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SendController {
    private static final String BASE_URL_SENDER = "/sender";


    SendEmailServiceImpl sendEmailService;

    public SendController(SendEmailServiceImpl sendEmailService) {
        this.sendEmailService = sendEmailService;
    }

    @PostMapping(BASE_URL_SENDER)

//    public String send(@RequestBody SenderDto senderDto) {
//
//        return "Вы находитесь в сервисе отправки сообщений Ваш email: " + senderDto.getEmail()+"зайдите снова и введите код из почты";
//    }
    public String send(@RequestBody SenderDto senderDto) {

        return sendEmailService.sendEmail(senderDto.getEmail(), senderDto.getCode());


    }
}
