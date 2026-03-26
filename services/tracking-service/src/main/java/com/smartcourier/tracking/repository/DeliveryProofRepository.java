package com.smartcourier.tracking.repository;

import com.smartcourier.tracking.domain.DeliveryProofEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryProofRepository extends JpaRepository<DeliveryProofEntity, Long> {
    Optional<DeliveryProofEntity> findByDeliveryId(Long deliveryId);
}
