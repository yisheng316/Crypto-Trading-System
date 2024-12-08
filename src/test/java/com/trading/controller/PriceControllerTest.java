package com.trading.controller;

import com.trading.model.Price;
import com.trading.service.PriceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceControllerTest {

    @Mock
    private PriceService priceService;

    @InjectMocks
    private PriceController priceController;

    private Price btcPrice;
    private Price ethPrice;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        btcPrice = new Price();
        btcPrice.setSymbol("BTCUSDT");
        btcPrice.setBidPrice(new BigDecimal("49995.00000000"));
        btcPrice.setAskPrice(new BigDecimal("50005.00000000"));
        btcPrice.setExchange("BINANCE");
        btcPrice.setTimestamp(now);

        ethPrice = new Price();
        ethPrice.setSymbol("ETHUSDT");
        ethPrice.setBidPrice(new BigDecimal("2995.00000000"));
        ethPrice.setAskPrice(new BigDecimal("3005.00000000"));
        ethPrice.setExchange("COINBASE");
        ethPrice.setTimestamp(now);
    }

    @Nested
    @DisplayName("GET /api/prices/latest/{symbol}")
    class GetLatestPrice {


        @Test
        @DisplayName("Should return price when symbol exists")
        void getLatestPrice_WhenSymbolExists_ShouldReturnPrice() {
            when(priceService.getLatestPrice("BTCUSDT")).thenReturn(Optional.of(btcPrice));

            ResponseEntity<Price> response = priceController.getLatestPrice("BTCUSDT");

            assertAll(
                    "Verify response properties",
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> assertEquals("BTCUSDT", Objects.requireNonNull(response.getBody()).getSymbol()),
                    () -> assertEquals(new BigDecimal("49995.00000000"),
                            Objects.requireNonNull(response.getBody()).getBidPrice()),
                    () -> assertEquals(new BigDecimal("50005.00000000"),
                            Objects.requireNonNull(response.getBody()).getAskPrice()),
                    () -> assertEquals("BINANCE", Objects.requireNonNull(response.getBody()).getExchange()),
                    () -> assertEquals(now, Objects.requireNonNull(response.getBody()).getTimestamp())
            );

            verify(priceService, times(1)).getLatestPrice("BTCUSDT");
        }

        @Test
        @DisplayName("Should return 404 when symbol doesn't exist")
        void getLatestPrice_WhenSymbolDoesNotExist_ShouldReturn404() {
            when(priceService.getLatestPrice("INVALID-PAIR")).thenReturn(Optional.empty());

            ResponseEntity<Price> response = priceController.getLatestPrice("INVALID-PAIR");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(response.getBody());

            verify(priceService, times(1)).getLatestPrice("INVALID-PAIR");
        }
    }

    @Nested
    @DisplayName("GET /api/prices/latest")
    class GetAllLatestPrices {

        @Test
        @DisplayName("Should return all available prices")
        void getAllLatestPrices_ShouldReturnAvailablePrices() {
            when(priceService.getLatestPrice("BTCUSDT")).thenReturn(Optional.of(btcPrice));
            when(priceService.getLatestPrice("ETHUSDT")).thenReturn(Optional.of(ethPrice));

            ResponseEntity<List<Price>> response = priceController.getAllLatestPrices();

            assertAll(
                    "Verify response properties",
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> assertEquals(2, Objects.requireNonNull(response.getBody()).size()),
                    () -> assertTrue(Objects.requireNonNull(response.getBody()).stream()
                            .anyMatch(p -> p.getSymbol().equals("BTCUSDT"))),
                    () -> assertTrue(Objects.requireNonNull(response.getBody()).stream()
                            .anyMatch(p -> p.getSymbol().equals("ETHUSDT")))
            );

            PriceService.TRADING_PAIRS.forEach(symbol ->
                    verify(priceService, times(1)).getLatestPrice(symbol));
        }

        @Test
        @DisplayName("Should handle when some prices are unavailable")
        void getAllLatestPrices_WhenSomePricesUnavailable_ShouldReturnAvailablePrices() {
            when(priceService.getLatestPrice("BTCUSDT")).thenReturn(Optional.of(btcPrice));
            when(priceService.getLatestPrice("ETHUSDT")).thenReturn(Optional.empty());

            ResponseEntity<List<Price>> response = priceController.getAllLatestPrices();

            assertAll(
                    "Verify response properties",
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> assertEquals(1, Objects.requireNonNull(response.getBody()).size()),
                    () -> assertEquals("BTCUSDT", Objects.requireNonNull(response.getBody()).get(0).getSymbol())
            );

            PriceService.TRADING_PAIRS.forEach(symbol ->
                    verify(priceService, times(1)).getLatestPrice(symbol));
        }
    }
}