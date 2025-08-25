package com.example.rent_module.service.impl;

import com.example.rent_module.dto.LocationDto;
import com.example.rent_module.dto.location_response.LocationResponseDto;
import com.example.rent_module.entity.IntegrationEntity;
import com.example.rent_module.repository.IntegrationRepository;
import com.example.rent_module.service.RentIntegrationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor
@Service
public class RentIntegrationServiceImpl implements RentIntegrationService {

    private final RestTemplate restTemplate;

    private final IntegrationRepository integrationRepository;

    @Override
    public LocationResponseDto geoIntegration(LocationDto dto) {

        return restTemplate.exchange(prepareUrlForGeo(dto),
                HttpMethod.GET,
                new HttpEntity<>(null, null),
                LocationResponseDto.class
        ).getBody();
    }

    private String prepareUrlForGeo(LocationDto dto) {
        IntegrationEntity value = integrationRepository.findById("GEO").get();
        String key = Base64EncodDecod.decode(value.getToken());
        return String.format(value.getPath(), dto.getLatitude(), dto.getLongitude(), key);

    }
}
