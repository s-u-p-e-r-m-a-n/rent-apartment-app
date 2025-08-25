package com.example.rent_module.dto.location_response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true, value = {"error"})
public class LocationResponseDto {
   private List<Results> results;

}
