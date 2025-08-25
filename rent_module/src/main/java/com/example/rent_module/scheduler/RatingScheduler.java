package com.example.rent_module.scheduler;

import com.example.rent_module.service.impl.RentServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
@RequiredArgsConstructor
@Slf4j
public class RatingScheduler {
    private final RentServiceImpl rentService;

    @Scheduled(fixedRate = 1000)
    private void apartmentRating() {
        //log.info("начал работу планировщик рентапартмент");

        rentService.schedulerRatingApartment();

    }

}
