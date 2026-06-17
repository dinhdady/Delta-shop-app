package com.hoangdinh.delta_shop_app.dto.response.wishlist;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class WishlistResponse {
    private UUID userId;
    private Integer totalItems;
    private boolean hasItems;
    private List<UUID> productIds;
}
