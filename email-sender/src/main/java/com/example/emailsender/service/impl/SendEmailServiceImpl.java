package com.example.emailsender.service.impl;

import com.example.emailsender.service.SendEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@RequiredArgsConstructor
@Service
public class SendEmailServiceImpl implements SendEmailService {

    private final static String SUBJECT = "Подтвердите свою почту на RENT_APARTMENT";
    private final static String TEXT_MAIL = "Пройдите авторизацию на RENT_APARTMENT и введите код: ";
    private final JavaMailSender mailSender;




    /**
     * метод отправки email уведомлений
     **/
    public String sendEmail(String email, String code) {

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(email);//кому
        simpleMailMessage.setSubject(SUBJECT);//тема
        simpleMailMessage.setText(TEXT_MAIL + code);//содержимое письма
        simpleMailMessage.setFrom("example@yandex.ru");
        mailSender.send(simpleMailMessage);
        return "проверьте Email";
    }
}
