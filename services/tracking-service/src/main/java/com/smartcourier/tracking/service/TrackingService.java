package com.smartcourier.tracking.service;

import com.smartcourier.tracking.domain.DeliveryProofEntity;
import com.smartcourier.tracking.domain.DocumentEntity;
import com.smartcourier.tracking.domain.TrackingEventEntity;
import com.smartcourier.tracking.repository.DeliveryProofRepository;
import com.smartcourier.tracking.repository.DocumentRepository;
import com.smartcourier.tracking.repository.TrackingEventRepository;
import com.smartcourier.tracking.web.dto.CreateProofRequest;
import com.smartcourier.tracking.web.dto.CreateTrackingEventRequest;
import com.smartcourier.tracking.web.dto.DeliveryProofResponse;
import com.smartcourier.tracking.web.dto.DocumentResponse;
import com.smartcourier.tracking.web.dto.TrackingEventResponse;
import com.smartcourier.tracking.web.dto.UploadDocumentRequest;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrackingService implements TrackingServicePort {

    private final TrackingEventRepository trackingEventRepository;
    private final DocumentRepository documentRepository;
    private final DeliveryProofRepository deliveryProofRepository;

    public TrackingService(
            TrackingEventRepository trackingEventRepository,
            DocumentRepository documentRepository,
            DeliveryProofRepository deliveryProofRepository) {
        this.trackingEventRepository = trackingEventRepository;
        this.documentRepository = documentRepository;
        this.deliveryProofRepository = deliveryProofRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> track(String trackingNumber) {
        List<TrackingEventResponse> events = byTracking(trackingNumber);
        TrackingEventResponse latest = events.isEmpty() ? null : events.getFirst();
        return Map.of(
                "trackingNumber", trackingNumber,
                "latestEvent", latest == null ? "NO_EVENTS" : latest,
                "eventCount", events.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackingEventResponse> byTracking(String trackingNumber) {
        return trackingEventRepository.findByTrackingNumberOrderByEventTimeDesc(trackingNumber).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TrackingEventResponse createEvent(CreateTrackingEventRequest request) {
        TrackingEventEntity entity = new TrackingEventEntity();
        entity.setDeliveryId(request.deliveryId());
        entity.setTrackingNumber(request.trackingNumber().trim());
        entity.setEventCode(request.eventCode().trim().toUpperCase());
        entity.setDescription(request.description().trim());
        entity.setEventTime(request.eventTime());
        return toResponse(trackingEventRepository.save(entity));
    }

    @Override
    @Transactional
    public DocumentResponse upload(UploadDocumentRequest request) {
        DocumentEntity doc = new DocumentEntity();
        doc.setDeliveryId(request.deliveryId());
        doc.setTrackingNumber(request.trackingNumber().trim());
        doc.setFileName(request.fileName().trim());
        doc.setFileType(request.fileType().trim());
        doc.setFileUrl(request.fileUrl().trim());
        return toResponse(documentRepository.save(doc));
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocument(Long id) {
        return toResponse(documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found")));
    }

    @Override
    @Transactional
    public Map<String, String> deleteDocument(Long id) {
        DocumentEntity entity = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        documentRepository.delete(entity);
        return Map.of("message", "Document deleted");
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryProofResponse getProof(Long deliveryId) {
        return toResponse(deliveryProofRepository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery proof not found")));
    }

    @Override
    @Transactional
    public DeliveryProofResponse createOrUpdateProof(Long deliveryId, CreateProofRequest request) {
        DeliveryProofEntity proof = deliveryProofRepository.findByDeliveryId(deliveryId).orElseGet(DeliveryProofEntity::new);
        proof.setDeliveryId(deliveryId);
        proof.setProofType(request.proofType().trim());
        proof.setProofUrl(request.proofUrl().trim());
        proof.setRecipientName(request.recipientName().trim());
        if (proof.getConfirmedAt() == null) {
            proof.setConfirmedAt(Instant.now());
        }
        return toResponse(deliveryProofRepository.save(proof));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> downloadProof(Long deliveryId) {
        DeliveryProofResponse proof = getProof(deliveryId);
        return Map.of("deliveryId", deliveryId, "downloadUrl", proof.proofUrl());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackingEventResponse> timeline(Long deliveryId) {
        return trackingEventRepository.findByDeliveryIdOrderByEventTimeDesc(deliveryId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingEventResponse latest(Long deliveryId) {
        return timeline(deliveryId).stream().findFirst().orElse(null);
    }

    private TrackingEventResponse toResponse(TrackingEventEntity e) {
        return new TrackingEventResponse(e.getId(), e.getDeliveryId(), e.getTrackingNumber(), e.getEventCode(), e.getDescription(), e.getEventTime(), e.getCreatedAt());
    }

    private DocumentResponse toResponse(DocumentEntity e) {
        return new DocumentResponse(e.getId(), e.getDeliveryId(), e.getTrackingNumber(), e.getFileName(), e.getFileType(), e.getFileUrl(), e.getUploadedAt());
    }

    private DeliveryProofResponse toResponse(DeliveryProofEntity e) {
        return new DeliveryProofResponse(e.getId(), e.getDeliveryId(), e.getProofType(), e.getProofUrl(), e.getRecipientName(), e.getConfirmedAt());
    }
}
