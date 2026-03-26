package com.smartcourier.auth.repository;

import com.smartcourier.auth.domain.RefreshTokenEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByTokenValue(String tokenValue);
    List<RefreshTokenEntity> findByUserIdAndRevokedAtIsNull(Long userId);
    void deleteByExpiresAtBefore(Instant cutoff);
}
