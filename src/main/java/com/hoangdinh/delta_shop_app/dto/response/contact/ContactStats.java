package com.hoangdinh.delta_shop_app.dto.response.contact;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContactStats {
    private long pending;
    private long processing;
    private long replied;
    private long resolved;
    private long closed;
    private long total;

    // Constructor mặc định
    public ContactStats() {}

    // Constructor với tham số
    public ContactStats(long pending, long processing, long replied, long resolved, long closed, long total) {
        this.pending = pending;
        this.processing = processing;
        this.replied = replied;
        this.resolved = resolved;
        this.closed = closed;
        this.total = total;
    }
}