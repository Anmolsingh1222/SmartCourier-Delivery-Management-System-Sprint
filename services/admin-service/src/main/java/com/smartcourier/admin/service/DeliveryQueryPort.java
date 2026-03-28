package com.smartcourier.admin.service;

import com.smartcourier.admin.web.dto.AdminDeliveryView;
import java.util.List;

public interface DeliveryQueryPort {

    List<AdminDeliveryView> findAllDeliveriesForAdmin();
}
