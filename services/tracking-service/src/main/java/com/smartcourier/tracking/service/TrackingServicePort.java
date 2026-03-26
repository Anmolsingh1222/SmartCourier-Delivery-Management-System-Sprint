package com.smartcourier.tracking.service;

import com.smartcourier.tracking.web.dto.CreateProofRequest;
import com.smartcourier.tracking.web.dto.CreateTrackingEventRequest;
import com.smartcourier.tracking.web.dto.DeliveryProofResponse;
import com.smartcourier.tracking.web.dto.DocumentResponse;
import com.smartcourier.tracking.web.dto.TrackingEventResponse;
import com.smartcourier.tracking.web.dto.UploadDocumentRequest;
import java.util.List;
import java.util.Map;

public interface TrackingServicePort {

    Map<String, Object> track(String trackingNumber);

    List<TrackingEventResponse> byTracking(String trackingNumber);

    TrackingEventResponse createEvent(CreateTrackingEventRequest request);

    DocumentResponse upload(UploadDocumentRequest request);

    DocumentResponse getDocument(Long id);

    Map<String, String> deleteDocument(Long id);

    DeliveryProofResponse getProof(Long deliveryId);

    DeliveryProofResponse createOrUpdateProof(Long deliveryId, CreateProofRequest request);

    Map<String, Object> downloadProof(Long deliveryId);

    List<TrackingEventResponse> timeline(Long deliveryId);

    TrackingEventResponse latest(Long deliveryId);
}
