package com.smartcourier.admin.repository;

import com.smartcourier.admin.domain.DeliveryExceptionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryExceptionRepository extends JpaRepository<DeliveryExceptionEntity, Long> {
    List<DeliveryExceptionEntity> findByResolvedFalseOrderByCreatedAtDesc();
}
