package com.hoangdinh.delta_shop_app.controller;

import com.hoangdinh.delta_shop_app.entity.ProductVariant;
import com.hoangdinh.delta_shop_app.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/variants")
@RequiredArgsConstructor
public class VariantController {

    private final ProductVariantRepository variantRepository;

    @GetMapping("/{variantId}")
    public ResponseEntity<?> getVariantInfo(@PathVariable UUID variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        return ResponseEntity.ok(Map.of(
                "id", variant.getId(),
                "productId", variant.getProduct().getId(),
                "productName", variant.getProduct().getName(),
                "sku", variant.getSku(),
                "price", variant.getFinalPrice()
        ));
    }
}