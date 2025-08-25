package com.example.rent_module.controller;

import com.example.rent_module.dto.ProductDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/*Внутренняя интеграция, обмен данными между сервисами, модуль rent выступает в качестве клиента делая запрос с помощью
restTemplate к product_module,ответ сервера сохраняется в result и передается в ответе*/

@RestController
public class TestIntegrationController {

    @GetMapping("/testintegration")
    public String testintegration() {
       RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.exchange(prepareUrl(),
                HttpMethod.GET,
                new HttpEntity<>(null, null),
                String.class).getBody();

        return result;

    }
    //подготовка url для запроса
    private String prepareUrl(){
        String param = "test";
        String key="test";
        String url= "http://localhost:8085/product?param=%s&key=%s";

        return String.format(url,param,key);
    }

    @GetMapping("/testintegration1")
    public String testintegration1() {
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.exchange(prepareUrl1(),
                HttpMethod.POST,
                new HttpEntity<>(new ProductDto("test","test"), prepareHeaders()),
                String.class).getBody();

        return result;

    }
    //Создание URL
        private String prepareUrl1(){

        String url= "http://localhost:8085/productcreate";

        return url;
    }
//Создание заголовка
    private HttpHeaders prepareHeaders(){

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("key", "test");
        return headers;
    }

}
