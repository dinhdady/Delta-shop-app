package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.request.shipping.ShippingFeeRequest;
import com.hoangdinh.delta_shop_app.dto.request.shipping.ShippingZoneRequest;
import com.hoangdinh.delta_shop_app.dto.response.shipping.ShippingFeeResponse;
import com.hoangdinh.delta_shop_app.dto.response.shipping.ShippingZoneResponse;
import com.hoangdinh.delta_shop_app.dto.response.shipping.TrackingResponse;
import com.hoangdinh.delta_shop_app.entity.Order;
import com.hoangdinh.delta_shop_app.exception.ResourceNotFoundException;
import com.hoangdinh.delta_shop_app.repository.OrderRepository;
import com.hoangdinh.delta_shop_app.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {
    private final OrderRepository orderRepository;
    private final ConcurrentHashMap<UUID, ShippingZoneResponse> zones = new ConcurrentHashMap<>();

    @Override
    public ShippingFeeResponse calculateShippingFee(ShippingFeeRequest request) {
        BigDecimal amount = request.getOrderAmount() != null ? request.getOrderAmount() : BigDecimal.ZERO;
        BigDecimal fee = amount.compareTo(BigDecimal.valueOf(500_000)) >= 0
                ? BigDecimal.ZERO : defaultFee(request.getProvince());
        return ShippingFeeResponse.builder()
                .method("STANDARD")
                .description("Giao hàng tiêu chuẩn")
                .fee(fee)
                .estimatedDays(isMajorCity(request.getProvince()) ? 2 : 4)
                .build();
    }

    @Override
    public ShippingFeeResponse calculateOrderShippingFee(Order order) {
        return ShippingFeeResponse.builder()
                .method("STANDARD")
                .description("Phí vận chuyển của đơn hàng")
                .fee(order.getShippingFee())
                .estimatedDays(isMajorCity(order.getShippingProvince()) ? 2 : 4)
                .build();
    }

    @Override
    public List<ShippingFeeResponse> getAllShippingMethods(ShippingFeeRequest request) {
        ShippingFeeResponse standard = calculateShippingFee(request);
        ShippingFeeResponse express = ShippingFeeResponse.builder()
                .method("EXPRESS")
                .description("Giao hàng nhanh")
                .fee(standard.getFee().add(BigDecimal.valueOf(30_000)))
                .estimatedDays(1)
                .build();
        return List.of(standard, express);
    }

    @Override
    public ShippingZoneResponse createShippingZone(ShippingZoneRequest request) {
        UUID id = UUID.randomUUID();
        ShippingZoneResponse response = mapZone(id, request);
        zones.put(id, response);
        return response;
    }

    @Override
    public ShippingZoneResponse updateShippingZone(UUID zoneId, ShippingZoneRequest request) {
        if (!zones.containsKey(zoneId)) throw new ResourceNotFoundException("ShippingZone", "id", zoneId);
        ShippingZoneResponse response = mapZone(zoneId, request);
        zones.put(zoneId, response);
        return response;
    }

    @Override
    public void deleteShippingZone(UUID zoneId) {
        zones.remove(zoneId);
    }

    @Override
    public List<ShippingZoneResponse> getAllShippingZones() {
        return new ArrayList<>(zones.values());
    }

    @Override
    public TrackingResponse trackOrder(String trackingNumber) {
        Order order = orderRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "trackingNumber", trackingNumber));
        return mapTracking(order);
    }

    @Override
    @Transactional
    public void updateTrackingInfo(UUID orderId, String trackingNumber) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        order.setTrackingNumber(trackingNumber);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void updateDeliveryStatus(String trackingNumber, String status) {
        Order order = orderRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "trackingNumber", trackingNumber));
        order.setStatus(com.hoangdinh.delta_shop_app.enums.OrderStatus.valueOf(status));
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public String createShipment(Order order) {
        String trackingNumber = order.getTrackingNumber();
        if (trackingNumber == null || trackingNumber.isBlank()) {
            trackingNumber = "DLT-SHIP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            order.setTrackingNumber(trackingNumber);
            orderRepository.save(order);
        }
        return trackingNumber;
    }

    @Override public void printShippingLabel(UUID orderId) { orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId)); }
    @Override public void cancelShipment(String trackingNumber) { updateDeliveryStatus(trackingNumber, "CANCELLED"); }
    @Override
    @Transactional
    public void processBulkShipping(List<UUID> orderIds) {
        orderIds.forEach(id -> orderRepository.findById(id)
                .ifPresent(order -> order.setStatus(com.hoangdinh.delta_shop_app.enums.OrderStatus.SHIPPED)));
        orderRepository.flush();
    }
    @Override public byte[] generateShippingManifest(List<UUID> orderIds) { return String.join("\n", orderIds.stream().map(UUID::toString).toList()).getBytes(java.nio.charset.StandardCharsets.UTF_8); }

    private BigDecimal defaultFee(String province) { return isMajorCity(province) ? BigDecimal.valueOf(25_000) : BigDecimal.valueOf(35_000); }
    private boolean isMajorCity(String province) { return province != null && (province.contains("Hồ Chí Minh") || province.contains("Hà Nội")); }
    private ShippingZoneResponse mapZone(UUID id, ShippingZoneRequest request) { return ShippingZoneResponse.builder().id(id).name(request.getName()).provinces(request.getProvinces()).baseFee(request.getBaseFee()).active(request.isActive()).build(); }
    private TrackingResponse mapTracking(Order order) { return TrackingResponse.builder().orderId(order.getId()).orderNumber(order.getOrderNumber()).trackingNumber(order.getTrackingNumber()).status(order.getStatus().name()).shippedAt(order.getShippedAt()).deliveredAt(order.getDeliveredAt()).build(); }
}
