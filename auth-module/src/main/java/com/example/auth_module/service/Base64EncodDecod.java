package com.example.auth_module.service;

import java.util.Base64;

public class Base64EncodDecod {

    public static String decode(String value) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] byteValue = decoder.decode(value);
        return new String(byteValue);


    }
    public static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());
    }
//    public static String encode(MultipartFile file) throws IOException {
//
//
//        return Base64.getEncoder().encodeToString(file.getBytes());
//    }
}

