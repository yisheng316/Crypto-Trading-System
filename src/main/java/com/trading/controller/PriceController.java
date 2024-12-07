package com.trading.controller;

import com.trading.model.Price;
import com.trading.service.PriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/prices")
@Tag(name = "Price API", description = "Endpoints for managing cryptocurrency prices")
@RequiredArgsConstructor
public class PriceController {
    private final PriceService priceService;

    @GetMapping("/latest/{symbol}")
    @Operation(summary = "Get latest price for a symbol")
    public ResponseEntity<Price> getLatestPrice(@PathVariable String symbol) {
        return priceService.getLatestPrice(symbol)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/latest")
    @Operation(summary = "Get all latest prices")
    public ResponseEntity<List<Price>> getAllLatestPrices() {
        List<Price> prices = PriceService.TRADING_PAIRS.stream()
                .map(priceService::getLatestPrice)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        return ResponseEntity.ok(prices);
    }
}
