package com.example.rent_module.repository;


import com.example.rent_module.entity.ApartmentImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApartmentImageRepository extends JpaRepository<ApartmentImageEntity,Long> {
}
