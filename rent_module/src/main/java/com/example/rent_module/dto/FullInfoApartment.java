package com.example.rent_module.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FullInfoApartment {

    private String roomCount;
    private String price;
    private String city;
    private String street;
    private String houseNumber;
}
