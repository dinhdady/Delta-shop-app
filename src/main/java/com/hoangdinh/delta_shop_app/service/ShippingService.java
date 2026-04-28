package com.hoangdinh.delta_shop_app.service;

import com.hoangdinh.delta_shop_app.dto.request.shipping.ShippingFeeRequest;
import com.hoangdinh.delta_shop_app.dto.request.shipping.ShippingZoneRequest;
import com.hoangdinh.delta_shop_app.dto.response.shipping.ShippingFeeResponse;
import com.hoangdinh.delta_shop_app.dto.response.shipping.ShippingZoneResponse;
import com.hoangdinh.delta_shop_app.dto.response.shipping.TrackingResponse;
import com.hoangdinh.delta_shop_app.entity.Order;

import java.util.List;
import java.util.UUID;

public interface ShippingService {

    // Fee calculation
    ShippingFeeResponse calculateShippingFee(ShippingFeeRequest request);
    ShippingFeeResponse calculateOrderShippingFee(Order order);
    List<ShippingFeeResponse> getAllShippingMethods(ShippingFeeRequest request);

    // Zone management (Admin)
    ShippingZoneResponse createShippingZone(ShippingZoneRequest request);
    ShippingZoneResponse updateShippingZone(UUID zoneId, ShippingZoneRequest request);
    void deleteShippingZone(UUID zoneId);
    List<ShippingZoneResponse> getAllShippingZones();

    // Tracking
    TrackingResponse trackOrder(String trackingNumber);
    void updateTrackingInfo(UUID orderId, String trackingNumber);
    void updateDeliveryStatus(String trackingNumber, String status);

    // Third-party integration
    String createShipment(Order order);
    void printShippingLabel(UUID orderId);
    void cancelShipment(String trackingNumber);

    // Batch operations
    void processBulkShipping(List<UUID> orderIds);
    byte[] generateShippingManifest(List<UUID> orderIds);
}