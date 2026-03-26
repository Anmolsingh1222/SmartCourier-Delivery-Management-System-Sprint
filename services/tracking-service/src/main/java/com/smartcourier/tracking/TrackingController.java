package com.smartcourier.tracking;

import com.smartcourier.tracking.security.RequestContextService;
import com.smartcourier.tracking.service.TrackingServicePort;
import com.smartcourier.tracking.web.dto.CreateProofRequest;
import com.smartcourier.tracking.web.dto.CreateTrackingEventRequest;
import com.smartcourier.tracking.web.dto.DeliveryProofResponse;
import com.smartcourier.tracking.web.dto.DocumentResponse;
import com.smartcourier.tracking.web.dto.TrackingEventResponse;
import com.smartcourier.tracking.web.dto.UploadDocumentRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    private final TrackingServicePort trackingService;
    private final RequestContextService requestContextService;

    public TrackingController(TrackingServicePort trackingService, RequestContextService requestContextService) {
        this.trackingService = trackingService;
        this.requestContextService = requestContextService;
    }

    @GetMapping("/{trackingNumber}")
    public Map<String, Object> track(@PathVariable("trackingNumber") String trackingNumber) { return trackingService.track(trackingNumber); }

    @GetMapping("/{trackingNumber}/events")
    public List<TrackingEventResponse> events(@PathVariable("trackingNumber") String trackingNumber) { return trackingService.byTracking(trackingNumber); }

    @PostMapping("/events")
    public TrackingEventResponse createEvent(
            @Valid @RequestBody CreateTrackingEventRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        requestContextService.assertAdminOrSystem(roleHeader);
        return trackingService.createEvent(request);
    }

    @PostMapping("/documents/upload")
    public DocumentResponse uploadDocument(@Valid @RequestBody UploadDocumentRequest request) { return trackingService.upload(request); }

    @GetMapping("/documents/{id}")
    public DocumentResponse document(@PathVariable("id") Long id) { return trackingService.getDocument(id); }

    @DeleteMapping("/documents/{id}")
    public Map<String, String> deleteDocument(@PathVariable("id") Long id) { return trackingService.deleteDocument(id); }

    @GetMapping("/{id}/proof")
    public DeliveryProofResponse proof(@PathVariable("id") Long id) { return trackingService.getProof(id); }

    @PostMapping("/{id}/proof")
    public DeliveryProofResponse createProof(
            @PathVariable("id") Long id,
            @Valid @RequestBody CreateProofRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        requestContextService.assertAdminOrSystem(roleHeader);
        return trackingService.createOrUpdateProof(id, request);
    }

    @GetMapping("/{id}/proof/download")
    public Map<String, Object> downloadProof(@PathVariable("id") Long id) { return trackingService.downloadProof(id); }

    @GetMapping("/{id}/timeline")
    public List<TrackingEventResponse> timeline(@PathVariable("id") Long id) { return trackingService.timeline(id); }

    @GetMapping("/{id}/latest")
    public TrackingEventResponse latest(@PathVariable("id") Long id) { return trackingService.latest(id); }

    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("status", "UP", "service", "tracking-service"); }
}
