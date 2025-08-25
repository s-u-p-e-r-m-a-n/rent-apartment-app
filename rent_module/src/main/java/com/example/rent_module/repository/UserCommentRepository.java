package com.example.rent_module.repository;

import com.example.rent_module.entity.UserCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCommentRepository extends JpaRepository<UserCommentEntity,Long> {
}
