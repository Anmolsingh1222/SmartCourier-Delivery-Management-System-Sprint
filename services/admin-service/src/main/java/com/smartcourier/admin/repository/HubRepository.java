package com.smartcourier.admin.repository;

import com.smartcourier.admin.domain.HubEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HubRepository extends JpaRepository<HubEntity, Long> {
}
