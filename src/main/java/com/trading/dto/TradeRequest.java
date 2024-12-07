package com.trading.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TradeRequest {
    private String symbol;
    private String type;
    private BigDecimal quantity;
}
