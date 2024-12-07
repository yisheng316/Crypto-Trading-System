package com.trading.dto;

import lombok.Data;

@Data
public class BinanceResponse {
    private String symbol;
    private String bidPrice;
    private String askPrice;
    private String bidQty;
    private String askQty;
}
