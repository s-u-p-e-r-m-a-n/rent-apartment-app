package com.example.auth_module.service.impl;

import com.example.auth_module.service.ValidService;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Service
@NoArgsConstructor
public class UserValidationImpl implements ValidService {

    public String validation(String email)  {

        String regex = "^[A-Za-z0-9+_.-]+@[a-z]+[\\.]+(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        return email;
    }

}
