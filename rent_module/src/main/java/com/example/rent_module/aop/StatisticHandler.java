package com.example.rent_module.aop;

import com.example.rent_module.dto.FullInfoApartment;
import com.example.rent_module.entity.ApartmentStatEntity;
import com.example.rent_module.repository.ApartmentStatRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
//@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class StatisticHandler {

    private final ApartmentStatRepository apartmentStatRepository;

    public static final String POINT_CUT = "execution(* com.example.rent_module.service.impl.RentServiceImpl.getApartmentInfo(..))";

    @AfterReturning(value = POINT_CUT, returning = "result")
    //JoinPoint-информация о перехваченном методе,с которым можно делать проверки под свои задачи
    public void catchStatisticInfoForApartment(JoinPoint joinPoint, Object result) {
        String name = joinPoint.getSignature().getName();
        FullInfoApartment value = (FullInfoApartment) result;
        apartmentStatRepository.save(prepareApartmentStatEntity(value));


    }

    private ApartmentStatEntity prepareApartmentStatEntity(FullInfoApartment dto) {
        return new ApartmentStatEntity(
                "были просмотрены апартаменты: " + dto.getCity() + dto.getStreet() + dto.getHouseNumber());

    }
}
