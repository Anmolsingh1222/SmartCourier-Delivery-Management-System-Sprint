package com.smartcourier.tracking.repository;

import com.smartcourier.tracking.domain.TrackingEventEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackingEventRepository extends JpaRepository<TrackingEventEntity, Long> {
    List<TrackingEventEntity> findByTrackingNumberOrderByEventTimeDesc(String trackingNumber);
    List<TrackingEventEntity> findByDeliveryIdOrderByEventTimeDesc(Long deliveryId);
}
