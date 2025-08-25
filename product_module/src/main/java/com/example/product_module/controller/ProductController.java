package com.example.product_module.controller;

import com.example.product_module.dto.ProductDto;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {


    @GetMapping("/product")
    public String getProduct(@RequestParam String param,@RequestParam String key) {

        return "Это сообщение с сервиса PRODUCT "+param+" "+key ;
    }

    @PostMapping("/productcreate")
    public String createProduct(@RequestBody ProductDto dto, @RequestHeader String key) {


        return "Это сообщение с сервиса PRODUCT "+dto+" "+key ;
    }

}
