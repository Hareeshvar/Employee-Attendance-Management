package com.hareeshvar.attendance.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hareeshvar.attendance.entity.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUserUserId(Long userId);

    List<RefreshToken> findByUserUserIdAndRevokedFalse(Long userId);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.replacedByTokenId = :replacedById WHERE r.id = :id AND r.revoked = false")
    int revokeTokenAtomic(@Param("id") Long id, @Param("replacedById") Long replacedById);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user.userId = :userId AND r.revoked = false")
    int revokeAllUserTokens(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now OR (r.revoked = true AND r.createdAt < :cutoff)")
    void deleteExpiredOrStaleTokens(@Param("now") LocalDateTime now, @Param("cutoff") LocalDateTime cutoff);
}
