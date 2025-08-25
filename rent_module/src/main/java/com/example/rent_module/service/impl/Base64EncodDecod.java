package com.example.rent_module.service.impl;

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
}

