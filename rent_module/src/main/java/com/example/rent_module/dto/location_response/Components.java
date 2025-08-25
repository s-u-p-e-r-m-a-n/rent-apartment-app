package com.example.rent_module.dto.location_response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true, value = {"error"})
@Data
public class Components {

    @JsonProperty(value = "_normalized_city")
    private String normalizedCity;
    private String city;
    private String state;
    private String country;

}
