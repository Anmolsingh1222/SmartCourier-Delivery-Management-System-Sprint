package com.smartcourier.admin.service;

import com.smartcourier.admin.domain.AdminUserEntity;
import com.smartcourier.admin.domain.DeliveryExceptionEntity;
import com.smartcourier.admin.domain.HubEntity;
import com.smartcourier.admin.web.dto.CreateExceptionRequest;
import com.smartcourier.admin.web.dto.CreateHubRequest;
import com.smartcourier.admin.web.dto.ResolveExceptionRequest;
import com.smartcourier.admin.web.dto.UpdateRoleRequest;
import java.util.List;
import java.util.Map;

public interface AdminServicePort {

    Map<String, Object> dashboard();

    List<DeliveryExceptionEntity> deliveries();

    DeliveryExceptionEntity delivery(Long id);

    DeliveryExceptionEntity resolve(Long id, ResolveExceptionRequest request);

    DeliveryExceptionEntity createException(CreateExceptionRequest request);

    Map<String, Object> reports();

    Map<String, Object> dailyReport();

    Map<String, Object> slaReport();

    List<AdminUserEntity> users();

    AdminUserEntity updateRole(Long id, UpdateRoleRequest request);

    List<HubEntity> hubs();

    HubEntity createHub(CreateHubRequest request);
}
