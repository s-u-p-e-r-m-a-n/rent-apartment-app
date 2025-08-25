package com.example.rent_module.repository;

import com.example.rent_module.entity.ApartmentInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ApartmentInfoRepository extends JpaRepository<ApartmentInfoEntity,Long> {
@Query(value = "select u from ApartmentInfoEntity u where u.comment is not empty ")
    public List<ApartmentInfoEntity> findByCommentNotEmptyJpql();
}
