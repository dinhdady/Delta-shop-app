package com.hoangdinh.delta_shop_app.service.impl;

import com.hoangdinh.delta_shop_app.dto.request.cart.AddToCartRequest;
import com.hoangdinh.delta_shop_app.entity.Product;
import com.hoangdinh.delta_shop_app.entity.ProductVariant;
import com.hoangdinh.delta_shop_app.repository.ProductRepository;
import com.hoangdinh.delta_shop_app.repository.UserRepository;
import com.hoangdinh.delta_shop_app.repository.WishlistRepository;
import com.hoangdinh.delta_shop_app.service.CartService;
import com.hoangdinh.delta_shop_app.dto.response.cart.CartResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WishlistServiceImplTest {

    @Mock private WishlistRepository wishlistRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CartService cartService;

    @Test
    void moveWishlistItemToCartAddsAvailableVariantAndKeepsWishlistItem() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID variantId = UUID.randomUUID();
        ProductVariant variant = ProductVariant.builder()
                .id(variantId)
                .isActive(true)
                .stockQuantity(5)
                .reservedQuantity(0)
                .build();
        Product product = Product.builder().id(productId).variants(List.of(variant)).build();
        when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.of(product));

        WishlistServiceImpl service = new WishlistServiceImpl(
                wishlistRepository, userRepository, productRepository, cartService);
        CartResponse expectedCart = CartResponse.builder().totalItems(1).build();
        when(cartService.addToCart(org.mockito.ArgumentMatchers.eq(userId), org.mockito.ArgumentMatchers.any(AddToCartRequest.class)))
                .thenReturn(expectedCart);

        CartResponse result = service.moveWishlistItemToCart(userId, productId);

        ArgumentCaptor<AddToCartRequest> request = ArgumentCaptor.forClass(AddToCartRequest.class);
        verify(cartService).addToCart(org.mockito.ArgumentMatchers.eq(userId), request.capture());
        assertThat(request.getValue().getVariantId()).isEqualTo(variantId);
        assertThat(request.getValue().getQuantity()).isEqualTo(1);
        assertThat(result).isSameAs(expectedCart);
        verify(wishlistRepository, never()).deleteByIdUserIdAndIdProductId(userId, productId);
    }
}
