package com.smartcourier.admin.repository;

import com.smartcourier.admin.domain.AdminUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserRepository extends JpaRepository<AdminUserEntity, Long> {
}
