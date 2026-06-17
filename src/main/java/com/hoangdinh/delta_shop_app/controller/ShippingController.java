package com.hoangdinh.delta_shop_app.controller;

import com.hoangdinh.delta_shop_app.dto.request.shipping.ShippingFeeRequest;
import com.hoangdinh.delta_shop_app.dto.request.shipping.ShippingZoneRequest;
import com.hoangdinh.delta_shop_app.dto.response.shipping.ShippingFeeResponse;
import com.hoangdinh.delta_shop_app.dto.response.shipping.ShippingZoneResponse;
import com.hoangdinh.delta_shop_app.dto.response.shipping.TrackingResponse;
import com.hoangdinh.delta_shop_app.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/shipping")
@RequiredArgsConstructor
public class ShippingController {
    private final ShippingService shippingService;

    @PostMapping("/fees")
    public ResponseEntity<List<ShippingFeeResponse>> calculateFees(@RequestBody ShippingFeeRequest request) {
        return ResponseEntity.ok(shippingService.getAllShippingMethods(request));
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<TrackingResponse> track(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(shippingService.trackOrder(trackingNumber));
    }

    @GetMapping("/admin/zones")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<ShippingZoneResponse>> zones() { return ResponseEntity.ok(shippingService.getAllShippingZones()); }

    @PostMapping("/admin/zones")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ShippingZoneResponse> createZone(@RequestBody ShippingZoneRequest request) { return ResponseEntity.ok(shippingService.createShippingZone(request)); }

    @PutMapping("/admin/zones/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ShippingZoneResponse> updateZone(@PathVariable UUID id, @RequestBody ShippingZoneRequest request) { return ResponseEntity.ok(shippingService.updateShippingZone(id, request)); }

    @DeleteMapping("/admin/zones/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteZone(@PathVariable UUID id) { shippingService.deleteShippingZone(id); return ResponseEntity.noContent().build(); }
}
