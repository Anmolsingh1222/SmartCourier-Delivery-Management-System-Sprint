package com.smartcourier.admin;

import com.smartcourier.admin.domain.AdminUserEntity;
import com.smartcourier.admin.domain.DeliveryExceptionEntity;
import com.smartcourier.admin.domain.HubEntity;
import com.smartcourier.admin.security.RequestContextService;
import com.smartcourier.admin.service.AdminServicePort;
import com.smartcourier.admin.web.dto.CreateExceptionRequest;
import com.smartcourier.admin.web.dto.CreateHubRequest;
import com.smartcourier.admin.web.dto.ResolveExceptionRequest;
import com.smartcourier.admin.web.dto.UpdateRoleRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminServicePort adminService;
    private final RequestContextService requestContextService;

    public AdminController(AdminServicePort adminService, RequestContextService requestContextService) {
        this.adminService = adminService;
        this.requestContextService = requestContextService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(@RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        requestContextService.assertAdmin(roleHeader);
        return adminService.dashboard();
    }

    @GetMapping("/deliveries")
    public List<DeliveryExceptionEntity> deliveries(@RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        requestContextService.assertAdmin(roleHeader);
        return adminService.deliveries();
    }

    @GetMapping("/deliveries/{id}")
    public DeliveryExceptionEntity delivery(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        requestContextService.assertAdmin(roleHeader);
        return adminService.delivery(id);
    }

    @PutMapping("/deliveries/{id}/resolve")
    public DeliveryExceptionEntity resolve(
            @PathVariable("id") Long id,
            @Valid @RequestBody ResolveExceptionRequest body,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        requestContextService.assertAdmin(roleHeader);
        return adminService.resolve(id, body);
    }

    @PostMapping("/deliveries/{id}/exception")
    public DeliveryExceptionEntity createException(
            @PathVariable("id") Long id,
            @Valid @RequestBody CreateExceptionRequest body,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        requestContextService.assertAdmin(roleHeader);
        CreateExceptionRequest request = new CreateExceptionRequest(id, body.status(), body.reason());
        return adminService.createException(request);
    }

    @GetMapping("/reports")
    public Map<String, Object> reports(@RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        requestContextService.assertAdmin(roleHeader);
        return adminService.reports();
    }

    @GetMapping("/reports/daily")
    public Map<String, Object> dailyReport(@RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        requestContextService.assertAdmin(roleHeader);
        return adminService.dailyReport();
    }

    @GetMapping("/reports/sla")
    public Map<String, Object> slaReport(@RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        requestContextService.assertAdmin(roleHeader);
        return adminService.slaReport();
    }

    @GetMapping("/users")
    public List<AdminUserEntity> users(@RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        requestContextService.assertAdmin(roleHeader);
        return adminService.users();
    }

    @PutMapping("/users/{id}/role")
    public AdminUserEntity updateRole(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateRoleRequest body,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        requestContextService.assertAdmin(roleHeader);
        return adminService.updateRole(id, body);
    }

    @GetMapping("/hubs")
    public List<HubEntity> hubs(@RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        requestContextService.assertAdmin(roleHeader);
        return adminService.hubs();
    }

    @PostMapping("/hubs")
    public HubEntity createHub(
            @Valid @RequestBody CreateHubRequest body,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        requestContextService.assertAdmin(roleHeader);
        return adminService.createHub(body);
    }
}
