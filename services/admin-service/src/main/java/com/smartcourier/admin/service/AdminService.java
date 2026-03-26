package com.smartcourier.admin.service;

import com.smartcourier.admin.domain.AdminUserEntity;
import com.smartcourier.admin.domain.DeliveryExceptionEntity;
import com.smartcourier.admin.domain.HubEntity;
import com.smartcourier.admin.repository.AdminUserRepository;
import com.smartcourier.admin.repository.DeliveryExceptionRepository;
import com.smartcourier.admin.repository.HubRepository;
import com.smartcourier.admin.web.dto.CreateExceptionRequest;
import com.smartcourier.admin.web.dto.CreateHubRequest;
import com.smartcourier.admin.web.dto.ResolveExceptionRequest;
import com.smartcourier.admin.web.dto.UpdateRoleRequest;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService implements AdminServicePort {

    private final HubRepository hubRepository;
    private final AdminUserRepository adminUserRepository;
    private final DeliveryExceptionRepository deliveryExceptionRepository;

    public AdminService(
            HubRepository hubRepository,
            AdminUserRepository adminUserRepository,
            DeliveryExceptionRepository deliveryExceptionRepository) {
        this.hubRepository = hubRepository;
        this.adminUserRepository = adminUserRepository;
        this.deliveryExceptionRepository = deliveryExceptionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> dashboard() {
        long hubs = hubRepository.count();
        long users = adminUserRepository.count();
        long openExceptions = deliveryExceptionRepository.findByResolvedFalseOrderByCreatedAtDesc().size();
        return Map.of(
                "hubs", hubs,
                "users", users,
                "openExceptions", openExceptions,
                "generatedAt", Instant.now().toString());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryExceptionEntity> deliveries() {
        return deliveryExceptionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryExceptionEntity delivery(Long id) {
        return deliveryExceptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery exception not found"));
    }

    @Override
    @Transactional
    public DeliveryExceptionEntity resolve(Long id, ResolveExceptionRequest request) {
        DeliveryExceptionEntity exception = deliveryExceptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery exception not found"));
        exception.setResolved(true);
        exception.setResolution(request.resolution().trim());
        exception.setResolvedAt(Instant.now());
        return deliveryExceptionRepository.save(exception);
    }

    @Override
    @Transactional
    public DeliveryExceptionEntity createException(CreateExceptionRequest request) {
        DeliveryExceptionEntity exception = new DeliveryExceptionEntity();
        exception.setDeliveryId(request.deliveryId());
        exception.setStatus(request.status().trim().toUpperCase());
        exception.setReason(request.reason().trim());
        return deliveryExceptionRepository.save(exception);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> reports() {
        return Map.of(
                "totalExceptions", deliveryExceptionRepository.count(),
                "resolved", deliveryExceptionRepository.findAll().stream().filter(DeliveryExceptionEntity::isResolved).count(),
                "open", deliveryExceptionRepository.findByResolvedFalseOrderByCreatedAtDesc().size());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> dailyReport() {
        return Map.of("date", Instant.now().toString(), "exceptionsLogged", deliveryExceptionRepository.count());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> slaReport() {
        long total = deliveryExceptionRepository.count();
        long resolved = deliveryExceptionRepository.findAll().stream().filter(DeliveryExceptionEntity::isResolved).count();
        double ratio = total == 0 ? 100.0 : (resolved * 100.0 / total);
        return Map.of("resolutionRatePercent", ratio, "sampleSize", total);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserEntity> users() {
        return adminUserRepository.findAll();
    }

    @Override
    @Transactional
    public AdminUserEntity updateRole(Long id, UpdateRoleRequest request) {
        AdminUserEntity user = adminUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRole(request.role().trim().toUpperCase());
        return adminUserRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HubEntity> hubs() {
        return hubRepository.findAll();
    }

    @Override
    @Transactional
    public HubEntity createHub(CreateHubRequest request) {
        HubEntity hub = new HubEntity();
        hub.setCode(request.code().trim().toUpperCase());
        hub.setName(request.name().trim());
        hub.setCity(request.city().trim());
        return hubRepository.save(hub);
    }
}
