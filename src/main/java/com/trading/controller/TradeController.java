package com.trading.controller;

import com.trading.dto.TradeRequest;
import com.trading.dto.TradeResponse;
import com.trading.model.Trade;
import com.trading.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
@Tag(name = "Trade API", description = "Endpoints for managing trades")
@RequiredArgsConstructor
public class TradeController {
    private final TradeService tradeService;

    @PostMapping
    @Operation(summary = "Execute a trade")
    public TradeResponse executeTrade(@RequestBody TradeRequest request) {
        return tradeService.executeTrade(1L, request);
    }

    @GetMapping
    @Operation(summary = "Get user's trading history")
    public List<Trade> getUserTrades() {
        return tradeService.getUserTrades(1L);
    }
}
