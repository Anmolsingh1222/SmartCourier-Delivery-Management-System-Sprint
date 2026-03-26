package com.smartcourier.tracking.repository;

import com.smartcourier.tracking.domain.DocumentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
    List<DocumentEntity> findByTrackingNumberOrderByUploadedAtDesc(String trackingNumber);
}
