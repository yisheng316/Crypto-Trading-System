package com.trading.controller;

import com.trading.model.Wallet;
import com.trading.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@Tag(name = "Wallet API", description = "Endpoints for managing wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @GetMapping
    @Operation(summary = "Get user's wallets")
    public ResponseEntity<List<Wallet>> getUserWallets() {
        List<Wallet> wallets = walletService.getUserWallets(1L);
        return ResponseEntity.ok(wallets);
    }
}