package com.trading.dto;

import lombok.Data;

import java.util.List;

@Data
public class HuobiWrapper {
    private String status;
    private List<HuobiPrice> data;
}
