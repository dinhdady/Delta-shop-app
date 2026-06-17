package com.hoangdinh.delta_shop_app.controller;

import com.hoangdinh.delta_shop_app.dto.request.promotion.PromotionCreateRequest;
import com.hoangdinh.delta_shop_app.dto.request.promotion.PromotionUpdateRequest;
import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.promotion.PromotionResponse;
import com.hoangdinh.delta_shop_app.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotion", description = "APIs for promotion management")
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all promotions")
    public ResponseEntity<PageResponse<PromotionResponse>> getPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(promotionService.getAllPromotions(page, size));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active promotions")
    public ResponseEntity<List<PromotionResponse>> getActivePromotions() {
        return ResponseEntity.ok(promotionService.getActivePromotions());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get promotion detail")
    public ResponseEntity<PromotionResponse> getPromotion(@PathVariable UUID id) {
        return ResponseEntity.ok(promotionService.getPromotion(id));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get promotion by code")
    public ResponseEntity<PromotionResponse> getPromotionByCode(@PathVariable String code) {
        return ResponseEntity.ok(promotionService.getPromotionByCode(code));
    }

    @GetMapping("/validate")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Validate promotion code")
    public ResponseEntity<?> validatePromotion(
            @RequestParam String code,
            @RequestParam BigDecimal subtotal,
            @RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(promotionService.validatePromotion(code, subtotal, userId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create promotion")
    public ResponseEntity<PromotionResponse> createPromotion(
            @Valid @RequestBody PromotionCreateRequest request,
            @RequestAttribute("userId") UUID adminId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promotionService.createPromotion(request, adminId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update promotion")
    public ResponseEntity<PromotionResponse> updatePromotion(
            @PathVariable UUID id,
            @RequestBody PromotionUpdateRequest request) {
        return ResponseEntity.ok(promotionService.updatePromotion(id, request));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Activate promotion")
    public ResponseEntity<Void> activatePromotion(@PathVariable UUID id) {
        promotionService.activatePromotion(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Deactivate promotion")
    public ResponseEntity<Void> deactivatePromotion(@PathVariable UUID id) {
        promotionService.deactivatePromotion(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete promotion")
    public ResponseEntity<Void> deletePromotion(@PathVariable UUID id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }
}
