package com.market.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class MarketData {
    private String symbol;
    private Float bid;
    private Float ask;
    private LocalDateTime updatedAt;
}
