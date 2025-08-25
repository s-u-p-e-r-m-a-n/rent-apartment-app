package com.example.rent_module.dto.location_response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true, value = {"error"})
@Data
public class Results {
    private Components components;
}
