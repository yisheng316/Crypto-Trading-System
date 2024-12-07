package com.trading.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TradeResponse {
    private Long tradeId;
    private String symbol;
    private String type;
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal total;
    private LocalDateTime timestamp;
}
