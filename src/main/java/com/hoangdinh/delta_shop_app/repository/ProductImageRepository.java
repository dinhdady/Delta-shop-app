package com.hoangdinh.delta_shop_app.repository;

import com.hoangdinh.delta_shop_app.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {
}