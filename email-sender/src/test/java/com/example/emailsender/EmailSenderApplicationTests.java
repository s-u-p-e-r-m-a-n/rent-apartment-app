package com.example.emailsender;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")                                        // <— важное
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class EmailSenderApplicationTests {

    @MockBean
    JavaMailSender mailSender; // закрывает зависимости сервисов от почты

    @Test
    void contextLoads() {}
}
