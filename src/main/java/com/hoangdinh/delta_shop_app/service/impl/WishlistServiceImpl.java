package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.response.PageResponse;
import com.hoangdinh.delta_shop_app.dto.response.product.ProductSummaryResponse;
import com.hoangdinh.delta_shop_app.dto.response.wishlist.WishlistResponse;
import com.hoangdinh.delta_shop_app.dto.request.cart.AddToCartRequest;
import com.hoangdinh.delta_shop_app.dto.response.cart.CartResponse;
import com.hoangdinh.delta_shop_app.entity.Product;
import com.hoangdinh.delta_shop_app.entity.ProductVariant;
import com.hoangdinh.delta_shop_app.entity.User;
import com.hoangdinh.delta_shop_app.entity.Wishlist;
import com.hoangdinh.delta_shop_app.exception.ResourceNotFoundException;
import com.hoangdinh.delta_shop_app.exception.BusinessException;
import com.hoangdinh.delta_shop_app.repository.ProductRepository;
import com.hoangdinh.delta_shop_app.repository.UserRepository;
import com.hoangdinh.delta_shop_app.repository.WishlistRepository;
import com.hoangdinh.delta_shop_app.service.WishlistService;
import com.hoangdinh.delta_shop_app.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    @Override
    @Transactional
    public WishlistResponse addToWishlist(UUID userId, UUID productId) {
        if (!wishlistRepository.existsByIdUserIdAndIdProductId(userId, productId)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

            wishlistRepository.save(Wishlist.builder()
                    .id(new Wishlist.WishlistId(userId, productId))
                    .user(user)
                    .product(product)
                    .build());
        }
        return getWishlistSummary(userId);
    }

    @Override
    @Transactional
    public void removeFromWishlist(UUID userId, UUID productId) {
        wishlistRepository.deleteByIdUserIdAndIdProductId(userId, productId);
    }

    @Override
    @Transactional
    public void clearWishlist(UUID userId) {
        wishlistRepository.deleteByIdUserId(userId);
    }

    @Override
    public PageResponse<ProductSummaryResponse> getUserWishlist(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductSummaryResponse> products = wishlistRepository.findByIdUserId(userId, pageable)
                .map(Wishlist::getProduct)
                .map(ProductSummaryResponse::from);
        return PageResponse.of(products);
    }

    @Override
    public WishlistResponse getWishlistSummary(UUID userId) {
        return WishlistResponse.builder()
                .userId(userId)
                .totalItems((int) wishlistRepository.countByIdUserId(userId))
                .hasItems(wishlistRepository.countByIdUserId(userId) > 0)
                .productIds(wishlistRepository.findProductIdsByUserId(userId))
                .build();
    }

    @Override
    public boolean isProductInWishlist(UUID userId, UUID productId) {
        return wishlistRepository.existsByIdUserIdAndIdProductId(userId, productId);
    }

    @Override
    public int getWishlistCount(UUID userId) {
        return (int) wishlistRepository.countByIdUserId(userId);
    }

    @Override
    @Transactional
    public void addMultipleToWishlist(UUID userId, List<UUID> productIds) {
        productIds.forEach(productId -> addToWishlist(userId, productId));
    }

    @Override
    @Transactional
    public void removeMultipleFromWishlist(UUID userId, List<UUID> productIds) {
        productIds.forEach(productId -> removeFromWishlist(userId, productId));
    }

    @Override
    @Transactional
    public CartResponse moveWishlistItemToCart(UUID userId, UUID productId) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        ProductVariant variant = product.getVariants().stream()
                .filter(ProductVariant::isActive)
                .filter(ProductVariant::isInStock)
                .findFirst()
                .orElseThrow(() -> new BusinessException("Sản phẩm hiện không còn biến thể có thể mua"));
        AddToCartRequest request = new AddToCartRequest();
        request.setVariantId(variant.getId());
        request.setQuantity(1);
        return cartService.addToCart(userId, request);
    }

    @Override
    @Transactional
    public CartResponse moveAllWishlistToCart(UUID userId) {
        List<UUID> productIds = wishlistRepository.findProductIdsByUserId(userId);
        productIds.forEach(productId -> {
            try {
                moveWishlistItemToCart(userId, productId);
            } catch (BusinessException ex) {
                // Skip unavailable products and keep the wishlist unchanged.
            }
        });
        return cartService.getCart(userId);
    }
}
