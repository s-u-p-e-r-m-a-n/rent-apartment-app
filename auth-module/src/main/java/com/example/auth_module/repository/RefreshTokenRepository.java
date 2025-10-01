package com.example.auth_module.repository;


import com.example.auth_module.model.RefreshToken;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    boolean existsByToken(String token);

    @Transactional
    @Modifying
    @Query("update RefreshToken t set t.revoked = true where t.id = :id and t.revoked = false")
    int markRevokedById(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("update RefreshToken t set t.revoked = true where t.user.id = :userId and t.revoked = false")
    int revokeAllByUserId(@Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query("delete from RefreshToken t where t.user.id = :userId and t.revoked = true")
    int deleteAllRevokedByUserId(@Param("userId") Long userId);
}
