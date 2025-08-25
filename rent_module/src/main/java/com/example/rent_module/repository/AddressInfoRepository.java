package com.example.rent_module.repository;

import com.example.rent_module.entity.AddressInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AddressInfoRepository extends JpaRepository<AddressInfoEntity,Long> {

    @Query(value = "select a from AddressInfoEntity a where a.city = :city")
    public Optional<List<AddressInfoEntity>> findByCity(String city);//обернули в optional для обработки исключения в сервис слое
}
