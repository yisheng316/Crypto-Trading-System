package com.trading.service;

import com.trading.dto.BinanceResponse;
import com.trading.dto.HuobiPrice;
import com.trading.dto.HuobiWrapper;
import com.trading.model.Price;
import com.trading.repository.PriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    @Mock
    private PriceRepository priceRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PriceService priceService;

    @Captor
    private ArgumentCaptor<Price> priceCaptor;

    private BinanceResponse btcBinance;
    private BinanceResponse ethBinance;
    private HuobiPrice btcHuobi;
    private HuobiPrice ethHuobi;

    @BeforeEach
    void setUp() {
        btcBinance = new BinanceResponse();
        btcBinance.setSymbol("BTCUSDT");
        btcBinance.setBidPrice("50000.00000000");
        btcBinance.setAskPrice("50100.00000000");

        ethBinance = new BinanceResponse();
        ethBinance.setSymbol("ETHUSDT");
        ethBinance.setBidPrice("3000.00000000");
        ethBinance.setAskPrice("3010.00000000");

        btcHuobi = new HuobiPrice();
        btcHuobi.setSymbol("btcusdt");
        btcHuobi.setBid(new BigDecimal("50050.00000000"));
        btcHuobi.setAsk(new BigDecimal("50150.00000000"));

        ethHuobi = new HuobiPrice();
        ethHuobi.setSymbol("ethusdt");
        ethHuobi.setBid(new BigDecimal("3005.00000000"));
        ethHuobi.setAsk(new BigDecimal("3015.00000000"));
    }

    @Nested
    @DisplayName("getLatestPrice tests")
    class GetLatestPriceTests {

        @Test
        @DisplayName("Should return price when found in repository")
        void getLatestPrice_WhenPriceExists_ShouldReturnPrice() {
            Price expectedPrice = new Price();
            expectedPrice.setSymbol("BTCUSDT");
            expectedPrice.setBidPrice(new BigDecimal("50000.00000000"));
            expectedPrice.setAskPrice(new BigDecimal("50100.00000000"));
            expectedPrice.setExchange("BINANCE");
            when(priceRepository.findLatestPriceBySymbol("BTCUSDT")).thenReturn(expectedPrice);

            Optional<Price> result = priceService.getLatestPrice("BTCUSDT");

            assertTrue(result.isPresent());
            assertEquals(expectedPrice, result.get());
            verify(priceRepository).findLatestPriceBySymbol("BTCUSDT");
        }

        @Test
        @DisplayName("Should return empty when price not found")
        void getLatestPrice_WhenPriceNotFound_ShouldReturnEmpty() {
            when(priceRepository.findLatestPriceBySymbol("INVALID")).thenReturn(null);

            Optional<Price> result = priceService.getLatestPrice("INVALID");

            assertFalse(result.isPresent());
            verify(priceRepository).findLatestPriceBySymbol("INVALID");
        }
    }

    @Nested
    @DisplayName("fetchAndSavePrices tests")
    class FetchAndSavePricesTests {

        @Test
        @DisplayName("Should save best prices when both exchanges return data")
        void fetchAndSavePrices_WhenBothExchangesRespond_ShouldSaveBestPrices() {
            BinanceResponse[] binanceResponses = {btcBinance, ethBinance};
            HuobiWrapper huobiWrapper = new HuobiWrapper();
            huobiWrapper.setData(Arrays.asList(btcHuobi, ethHuobi));

            when(restTemplate.getForEntity(anyString(), eq(BinanceResponse[].class)))
                    .thenReturn(ResponseEntity.ok(binanceResponses));
            when(restTemplate.getForEntity(anyString(), eq(HuobiWrapper.class)))
                    .thenReturn(ResponseEntity.ok(huobiWrapper));

            priceService.fetchAndSavePrices();

            verify(priceRepository, times(2)).save(priceCaptor.capture());
            List<Price> savedPrices = priceCaptor.getAllValues();

            assertAll("Verify saved prices",
                    () -> assertEquals(2, savedPrices.size()),
                    () -> {
                        Price btcPrice = savedPrices.stream()
                                .filter(p -> p.getSymbol().equals("BTCUSDT"))
                                .findFirst()
                                .orElseThrow();
                        assertEquals(new BigDecimal("50050.00000000"), btcPrice.getBidPrice(),
                                "Should use Huobi's higher bid price");
                        assertEquals(new BigDecimal("50100.00000000"), btcPrice.getAskPrice(),
                                "Should use Binance's lower ask price");
                        assertEquals("MIXED", btcPrice.getExchange());
                    }
            );
        }

        @Test
        @DisplayName("Should handle Binance-only data correctly")
        void fetchAndSavePrices_WhenOnlyBinanceResponds_ShouldSaveBinancePrices() {
            BinanceResponse[] binanceResponses = {btcBinance, ethBinance};
            when(restTemplate.getForEntity(anyString(), eq(BinanceResponse[].class)))
                    .thenReturn(ResponseEntity.ok(binanceResponses));
            when(restTemplate.getForEntity(anyString(), eq(HuobiWrapper.class)))
                    .thenReturn(ResponseEntity.ok(new HuobiWrapper()));

            priceService.fetchAndSavePrices();

            verify(priceRepository, times(2)).save(priceCaptor.capture());
            List<Price> savedPrices = priceCaptor.getAllValues();

            savedPrices.forEach(price -> {
                assertEquals("BINANCE", price.getExchange());
                assertNotNull(price.getTimestamp());
            });
        }

        @Test
        @DisplayName("Should handle API errors gracefully")
        void fetchAndSavePrices_WhenAPIsFail_ShouldHandleGracefully() {
            when(restTemplate.getForEntity(anyString(), eq(BinanceResponse[].class)))
                    .thenThrow(new RuntimeException("API Error"));
            when(restTemplate.getForEntity(anyString(), eq(HuobiWrapper.class)))
                    .thenThrow(new RuntimeException("API Error"));

            assertDoesNotThrow(() -> priceService.fetchAndSavePrices());
            verify(priceRepository, never()).save(any(Price.class));
        }
    }
}