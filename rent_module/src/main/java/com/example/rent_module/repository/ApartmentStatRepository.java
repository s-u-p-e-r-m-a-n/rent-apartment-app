package com.example.rent_module.repository;

import com.example.rent_module.entity.ApartmentStatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApartmentStatRepository extends JpaRepository<ApartmentStatEntity,Long> {

}
