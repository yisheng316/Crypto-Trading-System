package com.trading.controller;

import com.trading.dto.TradeRequest;
import com.trading.dto.TradeResponse;
import com.trading.model.Trade;
import com.trading.service.TradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeControllerTest {

    @Mock
    private TradeService tradeService;

    @InjectMocks
    private TradeController tradeController;

    private TradeRequest BUYTradeRequest;
    private TradeRequest SELLTradeRequest;
    private TradeResponse sampleTradeResponse;
    private List<Trade> sampleTrades;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        BUYTradeRequest = new TradeRequest();
        BUYTradeRequest.setSymbol("BTCUSDT");
        BUYTradeRequest.setType("BUY");
        BUYTradeRequest.setQuantity(new BigDecimal("0.5"));

        SELLTradeRequest = new TradeRequest();
        SELLTradeRequest.setSymbol("ETHUSDT");
        SELLTradeRequest.setType("SELL");
        SELLTradeRequest.setQuantity(new BigDecimal("2.0"));

        sampleTradeResponse = new TradeResponse();
        sampleTradeResponse.setTradeId(1L);
        sampleTradeResponse.setSymbol("BTCUSDT");
        sampleTradeResponse.setType("BUY");
        sampleTradeResponse.setQuantity(new BigDecimal("0.5"));
        sampleTradeResponse.setPrice(new BigDecimal("50000.00"));
        sampleTradeResponse.setTotal(new BigDecimal("25000.00"));
        sampleTradeResponse.setTimestamp(now);

        Trade trade1 = new Trade();
        trade1.setId(1L);
        trade1.setUserId(1L);
        trade1.setSymbol("BTCUSDT");
        trade1.setQuantity(new BigDecimal("0.5"));
        trade1.setPrice(new BigDecimal("50000.00"));
        trade1.setTimestamp(now.minusDays(1));

        Trade trade2 = new Trade();
        trade2.setId(2L);
        trade2.setUserId(1L);
        trade2.setSymbol("ETHUSDT");
        trade2.setQuantity(new BigDecimal("2.0"));
        trade2.setPrice(new BigDecimal("3000.00"));
        trade2.setTimestamp(now);

        sampleTrades = Arrays.asList(trade1, trade2);
    }

    @Nested
    @DisplayName("POST /api/trades")
    class ExecuteTradeTests {

        @Test
        @DisplayName("Should successfully execute a BUY trade")
        void executeTrade_WithBUYOrder_ShouldReturnTradeResponse() {
            when(tradeService.executeTrade(eq(1L), any(TradeRequest.class)))
                    .thenReturn(sampleTradeResponse);

            TradeResponse response = tradeController.executeTrade(BUYTradeRequest);

            assertAll("BUY trade response validation",
                    () -> assertNotNull(response, "Response should not be null"),
                    () -> assertEquals(1L, response.getTradeId(), "Trade ID should match"),
                    () -> assertEquals("BTCUSDT", response.getSymbol(), "Symbol should match"),
                    () -> assertEquals("BUY", response.getType(), "Trade type should be BUY"),
                    () -> assertEquals(new BigDecimal("0.5"), response.getQuantity(),
                            "Quantity should match"),
                    () -> assertEquals(now, response.getTimestamp(), "Timestamp should match")
            );

            verify(tradeService).executeTrade(eq(1L), eq(BUYTradeRequest));
        }

        @Test
        @DisplayName("Should successfully execute a SELL trade")
        void executeTrade_WithSELLOrder_ShouldReturnTradeResponse() {
            TradeResponse SELLResponse = new TradeResponse();
            SELLResponse.setTradeId(2L);
            SELLResponse.setSymbol("ETHUSDT");
            SELLResponse.setType("SELL");
            SELLResponse.setQuantity(new BigDecimal("2.0"));
            SELLResponse.setPrice(new BigDecimal("3000.00"));
            SELLResponse.setTotal(new BigDecimal("6000.00"));
            SELLResponse.setTimestamp(now);

            when(tradeService.executeTrade(eq(1L), any(TradeRequest.class)))
                    .thenReturn(SELLResponse);

            TradeResponse response = tradeController.executeTrade(SELLTradeRequest);

            assertAll("SELL trade response validation",
                    () -> assertEquals("ETHUSDT", response.getSymbol(), "Symbol should match"),
                    () -> assertEquals("SELL", response.getType(), "Trade type should be SELL"),
                    () -> assertEquals(new BigDecimal("2.0"), response.getQuantity(),
                            "Quantity should match")
            );

            verify(tradeService).executeTrade(eq(1L), eq(SELLTradeRequest));
        }
    }

    @Nested
    @DisplayName("GET /api/trades")
    class GetUserTradesTests {

        @Test
        @DisplayName("Should return user's trading history")
        void getUserTrades_ShouldReturnTradesList() {
            when(tradeService.getUserTrades(1L)).thenReturn(sampleTrades);

            List<Trade> trades = tradeController.getUserTrades();

            assertAll("Trade history validation",
                    () -> assertNotNull(trades, "Trades list should not be null"),
                    () -> assertEquals(2, trades.size(), "Should return correct number of trades"),
                    () -> assertEquals("BTCUSDT", trades.get(0).getSymbol(),
                            "First trade symbol should match"),
                    () -> assertEquals("ETHUSDT", trades.get(1).getSymbol(),
                            "Second trade symbol should match")
            );

            verify(tradeService).getUserTrades(1L);
        }

        @Test
        @DisplayName("Should handle empty trading history")
        void getUserTrades_WhenNoTrades_ShouldReturnEmptyList() {
            when(tradeService.getUserTrades(1L)).thenReturn(List.of());

            List<Trade> trades = tradeController.getUserTrades();

            assertAll("Empty trade history validation",
                    () -> assertNotNull(trades, "Trades list should not be null"),
                    () -> assertTrue(trades.isEmpty(), "Trades list should be empty")
            );

            verify(tradeService).getUserTrades(1L);
        }
    }
}