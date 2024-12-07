package com.trading.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HuobiPrice {
    private String symbol;
    private BigDecimal bid;
    private BigDecimal ask;
    private BigDecimal bidSize;
    private BigDecimal askSize;
}
