package com.example.rent_module.service;


import com.example.rent_module.dto.LocationDto;
import com.example.rent_module.dto.location_response.LocationResponseDto;

public interface RentIntegrationService {

    public LocationResponseDto geoIntegration(LocationDto dto);


}
