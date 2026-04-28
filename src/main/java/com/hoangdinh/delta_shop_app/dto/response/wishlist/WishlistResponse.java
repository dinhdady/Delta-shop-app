package com.hoangdinh.delta_shop_app.dto.response.wishlist;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WishlistResponse {
    private Integer totalItems;
    private boolean hasItems;
}