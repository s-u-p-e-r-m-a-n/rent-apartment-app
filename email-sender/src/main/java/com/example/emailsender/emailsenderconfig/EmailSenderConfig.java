package com.example.emailsender.emailsenderconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;
@Configuration
public class EmailSenderConfig {

    @Value("${spring.mail.host}")
    private String host;
    @Value("${spring.mail.username}")
    public static String username;
    @Value("${spring.mail.password}")
    private String password;
    @Value("${spring.mail.port}")
    private int port;
    @Value("${spring.mail.protocol}")
    private String protocol;
    @Value("${spring.mail.debug}")
    private String debug;

    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(host);
        javaMailSender.setUsername("foryouco@yandex.ru");
        javaMailSender.setPassword("nuuoujewrayjutbb");
        javaMailSender.setPort(port);
        Properties javaMailProperties = javaMailSender.getJavaMailProperties();
        javaMailProperties.put("mail.transport.protocol", protocol);
        javaMailProperties.put("mail.debug", debug);
        return javaMailSender;

    }
}
