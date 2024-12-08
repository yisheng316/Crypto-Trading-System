package com.trading.service;

import com.trading.dto.TradeRequest;
import com.trading.dto.TradeResponse;
import com.trading.exception.InsufficientBalanceException;
import com.trading.exception.InvalidTradeException;
import com.trading.exception.PriceNotFoundException;
import com.trading.model.Price;
import com.trading.model.Trade;
import com.trading.model.Wallet;
import com.trading.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private PriceService priceService;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private TradeService tradeService;

    private TradeRequest BUYTradeRequest;
    private TradeRequest SELLTradeRequest;
    private Trade sampleTrade;
    private Price samplePrice;
    private Wallet sampleUSDTWallet;
    private Wallet sampleBTCWallet;

    @BeforeEach
    void setUp() {
        BUYTradeRequest = new TradeRequest();
        BUYTradeRequest.setSymbol("BTCUSDT");
        BUYTradeRequest.setType("BUY");
        BUYTradeRequest.setQuantity(new BigDecimal("0.5"));

        SELLTradeRequest = new TradeRequest();
        SELLTradeRequest.setSymbol("BTCUSDT");
        SELLTradeRequest.setType("SELL");
        SELLTradeRequest.setQuantity(new BigDecimal("2.0"));

        sampleTrade = new Trade();
        sampleTrade.setId(1L);
        sampleTrade.setUserId(1L);
        sampleTrade.setSymbol("BTCUSDT");
        sampleTrade.setType("BUY");
        sampleTrade.setQuantity(new BigDecimal("0.5"));
        sampleTrade.setPrice(new BigDecimal("50000.00"));
        sampleTrade.setTotal(new BigDecimal("25000.00"));
        sampleTrade.setTimestamp(LocalDateTime.now());

        samplePrice = new Price();
        samplePrice.setSymbol("BTCUSDT");
        samplePrice.setAskPrice(new BigDecimal("50000.00"));
        samplePrice.setBidPrice(new BigDecimal("49500.00"));
        
        sampleUSDTWallet = new Wallet();
        sampleUSDTWallet.setId(1L);
        sampleUSDTWallet.setUserId(1L);
        sampleUSDTWallet.setCurrency("USDT");
        sampleUSDTWallet.setBalance(new BigDecimal("100000.00"));

        sampleBTCWallet = new Wallet();
        sampleBTCWallet.setId(2L);
        sampleBTCWallet.setUserId(1L);
        sampleBTCWallet.setCurrency("BTC");
        sampleBTCWallet.setBalance(new BigDecimal("200000.00"));
    }

    @Test
    @DisplayName("Should execute a successful BUY trade")
    void executeTrade_WithBUYOrder_ShouldReturnTradeResponse() {
        when(priceService.getLatestPrice(eq("BTCUSDT"))).thenReturn(Optional.of(samplePrice));
        when(walletService.getWalletByCurrency(eq(1L), eq("USDT"))).thenReturn(Optional.of(sampleUSDTWallet));
        doNothing().when(walletService).updateBalance(eq(1L), eq("USDT"), any(BigDecimal.class));
        doNothing().when(walletService).updateBalance(eq(1L), eq("BTC"), any(BigDecimal.class));
        when(tradeRepository.save(any(Trade.class))).thenReturn(sampleTrade);

        TradeResponse response = tradeService.executeTrade(1L, BUYTradeRequest);

        assertAll("BUY trade response validation",
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.getTradeId()).isEqualTo(1L),
                () -> assertThat(response.getSymbol()).isEqualTo("BTCUSDT"),
                () -> assertThat(response.getType()).isEqualTo("BUY"),
                () -> assertThat(response.getQuantity()).isEqualTo(new BigDecimal("0.5")),
                () -> assertThat(response.getPrice()).isEqualTo(new BigDecimal("50000.00")),
                () -> assertThat(response.getTotal()).isEqualTo(new BigDecimal("25000.00"))
        );
    }

    @Test
    @DisplayName("Should execute a successful SELL trade")
    void executeTrade_WithSELLOrder_ShouldReturnTradeResponse() {
        sampleTrade.setType("SELL");
        sampleTrade.setQuantity(new BigDecimal("2.0"));
        sampleTrade.setTotal(new BigDecimal("100000.00"));

        when(priceService.getLatestPrice(eq("BTCUSDT"))).thenReturn(Optional.of(samplePrice));
        when(walletService.getWalletByCurrency(eq(1L), eq("BTC"))).thenReturn(Optional.of(sampleBTCWallet));
        doNothing().when(walletService).updateBalance(eq(1L), eq("BTC"), any(BigDecimal.class));
        doNothing().when(walletService).updateBalance(eq(1L), eq("USDT"), any(BigDecimal.class));
        when(tradeRepository.save(any(Trade.class))).thenReturn(sampleTrade);

        TradeResponse response = tradeService.executeTrade(1L, SELLTradeRequest);

        assertAll("SELL trade response validation",
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.getTradeId()).isEqualTo(1L),
                () -> assertThat(response.getSymbol()).isEqualTo("BTCUSDT"),
                () -> assertThat(response.getType()).isEqualTo("SELL"),
                () -> assertThat(response.getQuantity()).isEqualTo(new BigDecimal("2.0")),
                () -> assertThat(response.getPrice()).isEqualTo(new BigDecimal("50000.00")),
                () -> assertThat(response.getTotal()).isEqualTo(new BigDecimal("100000.00"))
        );
    }

    @Test
    @DisplayName("Should throw InsufficientBalanceException for BUY trade")
    void executeTrade_WithInsufficientUSDTBalance_ShouldThrowException() {
        Wallet insufficientUSDTWallet = new Wallet();
        insufficientUSDTWallet.setId(1L);
        insufficientUSDTWallet.setUserId(1L);
        insufficientUSDTWallet.setCurrency("USDT");
        insufficientUSDTWallet.setBalance(new BigDecimal("20000.00"));

        when(priceService.getLatestPrice(eq("BTCUSDT"))).thenReturn(Optional.of(samplePrice));
        when(walletService.getWalletByCurrency(eq(1L), eq("USDT"))).thenReturn(Optional.of(insufficientUSDTWallet));

        assertThrows(InsufficientBalanceException.class, () -> tradeService.executeTrade(1L, BUYTradeRequest));
    }

    @Test
    @DisplayName("Should throw InsufficientBalanceException for SELL trade")
    void executeTrade_WithInsufficientCryptoBalance_ShouldThrowException() {
        Wallet insufficientBTCWallet = new Wallet();
        insufficientBTCWallet.setId(2L);
        insufficientBTCWallet.setUserId(1L);
        insufficientBTCWallet.setCurrency("BTC");
        insufficientBTCWallet.setBalance(new BigDecimal("0.5"));

        when(priceService.getLatestPrice(eq("BTCUSDT"))).thenReturn(Optional.of(samplePrice));
        when(walletService.getWalletByCurrency(eq(1L), eq("BTC"))).thenReturn(Optional.of(insufficientBTCWallet));

        assertThrows(InsufficientBalanceException.class, () -> tradeService.executeTrade(1L, SELLTradeRequest));
    }

    @Test
    @DisplayName("Should throw PriceNotFoundException when no price is available")
    void executeTrade_WithoutAvailablePrice_ShouldThrowException() {
        when(priceService.getLatestPrice(eq("BTCUSDT"))).thenReturn(Optional.empty());

        assertThrows(PriceNotFoundException.class, () -> tradeService.executeTrade(1L, BUYTradeRequest));
    }

    @Test
    @DisplayName("Should throw InvalidTradeException for invalid trade request")
    void executeTrade_WithInvalidTradeRequest_ShouldThrowException() {
        TradeRequest invalidRequest = new TradeRequest();
        invalidRequest.setSymbol(null);
        invalidRequest.setType("INVALID");
        invalidRequest.setQuantity(new BigDecimal("-1"));

        assertThrows(InvalidTradeException.class, () -> tradeService.executeTrade(1L, invalidRequest));
    }
}