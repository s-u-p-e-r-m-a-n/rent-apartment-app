package com.example.rent_module.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
public class СommentRequestDto {
    private String comment;
    private Integer grade;
    private Long apartmentId;
    private UserRequestDto user;
}
