package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.request.cart.AddToCartRequest;
import com.hoangdinh.delta_shop_app.dto.request.cart.UpdateCartItemRequest;
import com.hoangdinh.delta_shop_app.dto.response.cart.CartItemResponse;
import com.hoangdinh.delta_shop_app.dto.response.cart.CartResponse;
import com.hoangdinh.delta_shop_app.dto.response.cart.CartSummaryResponse;
import com.hoangdinh.delta_shop_app.entity.*;
import com.hoangdinh.delta_shop_app.exception.BusinessException;
import com.hoangdinh.delta_shop_app.exception.ResourceNotFoundException;
import com.hoangdinh.delta_shop_app.repository.CartRepository;
import com.hoangdinh.delta_shop_app.repository.ProductVariantRepository;
import com.hoangdinh.delta_shop_app.repository.UserRepository;
import com.hoangdinh.delta_shop_app.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository variantRepository;

    @Override
    public CartResponse getCart(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        return mapToResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(UUID userId, AddToCartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id", request.getVariantId()));

        // Check stock
        if (variant.getAvailableQuantity() < request.getQuantity()) {
            throw new BusinessException("Sản phẩm không đủ số lượng. Còn lại: " + variant.getAvailableQuantity());
        }

        Cart cart = getOrCreateCart(userId);

        // Check if item with same variant AND same size already exists
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getVariant().getId().equals(request.getVariantId()))
                .filter(item -> {
                    // So sánh size
                    if (request.getSelectedSize() != null) {
                        return request.getSelectedSize().equals(item.getSelectedSize());
                    }
                    return item.getSelectedSize() == null;
                })
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            if (variant.getAvailableQuantity() < newQuantity) {
                throw new BusinessException("Số lượng vượt quá tồn kho. Còn lại: " + variant.getAvailableQuantity());
            }
            item.setQuantity(newQuantity);
            item.setUpdatedAt(LocalDateTime.now());
        } else {
            // Add new item with size info
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(request.getQuantity())
                    .unitPrice(variant.getFinalPrice())
                    .selectedSize(request.getSelectedSize())
                    .selectedSizeLabel(request.getSelectedSizeLabel())
                    .selectedSizeMeasurement(request.getSelectedSizeMeasurement())
                    .build();
            cart.getItems().add(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        Cart saved = cartRepository.save(cart);

        log.info("Added to cart: user {}, variant {}, size {}, quantity {}",
                userId, request.getVariantId(), request.getSelectedSizeLabel(), request.getQuantity());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(UUID userId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(request.getCartItemId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", request.getCartItemId()));

        ProductVariant variant = cartItem.getVariant();

        if (request.getQuantity() <= 0) {
            // Remove item
            cart.getItems().remove(cartItem);
        } else {
            // Check stock
            if (variant.getAvailableQuantity() < request.getQuantity()) {
                throw new BusinessException("Sản phẩm không đủ số lượng. Còn lại: " + variant.getAvailableQuantity());
            }
            cartItem.setQuantity(request.getQuantity());
            cartItem.setUpdatedAt(LocalDateTime.now());
        }

        cart.setUpdatedAt(LocalDateTime.now());
        Cart saved = cartRepository.save(cart);

        log.info("Updated cart item: user {}, item {}, quantity {}", userId, request.getCartItemId(), request.getQuantity());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public CartResponse removeCartItem(UUID userId, UUID cartItemId) {
        Cart cart = getOrCreateCart(userId);

        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        cart.setUpdatedAt(LocalDateTime.now());

        Cart saved = cartRepository.save(cart);
        log.info("Removed cart item: user {}, item {}", userId, cartItemId);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void clearCart(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
        log.info("Cleared cart: user {}", userId);
    }

    @Override
    public CartSummaryResponse getCartSummary(UUID userId) {
        return null;
    }

    @Override
    public CartResponse getGuestCart(String sessionId) {
        return null;
    }

    @Override
    public CartResponse addToGuestCart(String sessionId, AddToCartRequest request) {
        return null;
    }

    @Override
    public CartResponse mergeGuestCart(UUID userId, String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return getCart(userId);
        }

        Optional<Cart> guestCartOpt = cartRepository.findBySessionId(sessionId);
        if (guestCartOpt.isEmpty()) {
            return getCart(userId);
        }

        Cart guestCart = guestCartOpt.get();
        Cart userCart = getOrCreateCart(userId);

        // Merge items from guest cart to user cart
        for (CartItem guestItem : guestCart.getItems()) {
            Optional<CartItem> existingItem = userCart.getItems().stream()
                    .filter(item -> item.getVariant().getId().equals(guestItem.getVariant().getId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                existingItem.get().setQuantity(existingItem.get().getQuantity() + guestItem.getQuantity());
                existingItem.get().setUpdatedAt(LocalDateTime.now());
            } else {
                CartItem newItem = CartItem.builder()
                        .cart(userCart)
                        .variant(guestItem.getVariant())
                        .quantity(guestItem.getQuantity())
                        .unitPrice(guestItem.getUnitPrice())
                        .build();
                userCart.getItems().add(newItem);
            }
        }

        userCart.setUpdatedAt(LocalDateTime.now());
        Cart saved = cartRepository.save(userCart);

        // Delete guest cart
        cartRepository.delete(guestCart);

        log.info("Merged guest cart to user cart: user {}", userId);

        return mapToResponse(saved);
    }

    @Override
    public void validateCartItems(UUID userId) {
        Cart cart = getOrCreateCart(userId);

        for (CartItem item : cart.getItems()) {
            ProductVariant variant = item.getVariant();
            if (variant.getAvailableQuantity() < item.getQuantity()) {
                throw new BusinessException("Sản phẩm '" + variant.getProduct().getName() +
                        "' không đủ số lượng. Còn lại: " + variant.getAvailableQuantity());
            }
        }
    }

    @Override
    public void validateStock(UUID variantId, int quantity) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id", variantId));

        if (variant.getAvailableQuantity() < quantity) {
            throw new BusinessException("Sản phẩm không đủ số lượng. Còn lại: " + variant.getAvailableQuantity());
        }
    }

    @Override
    public CartResponse prepareCartForCheckout(UUID userId) {
        validateCartItems(userId);
        return getCart(userId);
    }

    @Override
    @Transactional
    public void lockCartForCheckout(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        cart.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse mapToResponse(Cart cart) {
        BigDecimal subtotal = cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(cart.getItems().stream()
                        .map(item -> {
                            // Lấy URL ảnh từ nhiều nguồn
                            String productImage = null;

                            // Thử lấy từ variant imageUrl
                            if (item.getVariant().getImageUrl() != null && !item.getVariant().getImageUrl().isEmpty()) {
                                productImage = item.getVariant().getImageUrl();
                            }
                            // Nếu không, thử lấy từ product images
                            else if (item.getVariant().getProduct().getImages() != null &&
                                    !item.getVariant().getProduct().getImages().isEmpty()) {
                                // Lấy ảnh primary hoặc ảnh đầu tiên
                                productImage = item.getVariant().getProduct().getImages().stream()
                                        .filter(ProductImage::isPrimary)
                                        .findFirst()
                                        .orElse(item.getVariant().getProduct().getImages().get(0))
                                        .getUrl();
                            }
                            // Nếu vẫn không có, dùng ảnh mặc định
                            else {
                                productImage = "https://via.placeholder.com/100x100?text=No+Image";
                            }

                            return CartItemResponse.builder()
                                    .id(item.getId())
                                    .variantId(item.getVariant().getId())
                                    .productId(item.getVariant().getProduct().getId())
                                    .productName(item.getVariant().getProduct().getName())
                                    .variantName(item.getVariant().getName())
                                    .productImage(productImage)  // Đã fix
                                    .quantity(item.getQuantity())
                                    .unitPrice(item.getUnitPrice())
                                    .subtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                                    .availableStock(item.getVariant().getAvailableQuantity())
                                    .build();
                        })
                        .collect(Collectors.toList()))
                .totalItems(cart.getItems().size())
                .subtotal(subtotal)
                .discount(BigDecimal.ZERO)
                .total(subtotal)
                .build();
    }
}