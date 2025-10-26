package com.example.auth_module.unit.service;

import com.example.auth_module.dto.SenderDto;
import com.example.auth_module.exception.UserException;
import com.example.auth_module.service.impl.EmailSenderIntegrationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class EmailSenderIntegrationServiceTest {

    @Mock
    RestTemplate restTemplate;
    @InjectMocks
    EmailSenderIntegrationServiceImpl emailSenderIntegrationService;

    @Test
    @DisplayName("Успешная отправка кода на E-mail")
    void sendCodeVerification_ok_returnsString() {
        SenderDto dto = new SenderDto("test@mail.ru", "1234");
        String expectedResponse = "код отправлен";
        when(restTemplate.postForObject(anyString(), eq(dto), eq(String.class)))
            .thenReturn(expectedResponse);
        var result = emailSenderIntegrationService.sendCodeVerification(dto);
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(restTemplate, times(1))
            .postForObject(anyString(), eq(dto), eq(String.class));

    }

    @Test
    @DisplayName("ошибка отправки кода")
    void sendCodeVerification_fail_return_rest_exception() {
        SenderDto dto = new SenderDto("test@mail.ru", "1234");
        when(restTemplate.postForObject(anyString(), eq(dto), eq(String.class)))
            .thenThrow(new RestClientException("Service unavailable"));

        var ex= assertThrows(RestClientException.class,()->emailSenderIntegrationService.sendCodeVerification(dto));
       assertEquals("Service unavailable", ex.getMessage());

        verify(restTemplate, times(1)).postForObject(anyString(), eq(dto), eq(String.class));
    }

    @Test
    @DisplayName("ошибка отправки кода")
    void sendCodeVerification_fail_null_return_exception() {
        SenderDto dto = new SenderDto("test@mail.ru", "1234");
        when(restTemplate.postForObject(anyString(), eq(dto), eq(String.class)))
            .thenReturn(null);

       assertEquals(null,emailSenderIntegrationService.sendCodeVerification(dto));


        verify(restTemplate, times(1)).postForObject(anyString(), eq(dto), eq(String.class));
    }


}
