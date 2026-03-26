package com.smartcourier.delivery.repository;

import com.smartcourier.delivery.domain.DeliveryEntity;
import com.smartcourier.delivery.domain.DeliveryStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<DeliveryEntity, Long> {
    List<DeliveryEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    Optional<DeliveryEntity> findByTrackingNumber(String trackingNumber);
    List<DeliveryEntity> findByStatusOrderByUpdatedAtDesc(DeliveryStatus status);
}
