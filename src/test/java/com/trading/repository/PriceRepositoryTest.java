package com.trading.repository;

import com.trading.model.Price;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PriceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PriceRepository priceRepository;

    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        baseTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        createAndPersistPrice("BTCUSDT", "49900.00000000", "50100.00000000",
                "BINANCE", baseTime.minusMinutes(5));
        createAndPersistPrice("BTCUSDT", "50000.00000000", "50200.00000000",
                "BINANCE", baseTime.minusMinutes(3));
        createAndPersistPrice("BTCUSDT", "50100.00000000", "50300.00000000",
                "HUOBI", baseTime);

        createAndPersistPrice("ETHUSDT", "2995.00000000", "3005.00000000",
                "BINANCE", baseTime.minusMinutes(2));
        createAndPersistPrice("ETHUSDT", "3000.00000000", "3010.00000000",
                "HUOBI", baseTime.minusMinutes(1));
    }

    private void createAndPersistPrice(String symbol, String bidPrice, String askPrice,
                                       String exchange, LocalDateTime timestamp) {
        Price price = new Price();
        price.setSymbol(symbol);
        price.setBidPrice(new BigDecimal(bidPrice));
        price.setAskPrice(new BigDecimal(askPrice));
        price.setExchange(exchange);
        price.setTimestamp(timestamp);

        entityManager.persist(price);
    }

    @Test
    @DisplayName("Should find the most recent price for a given symbol")
    void findLatestPriceBySymbol_ShouldReturnMostRecentPrice() {
        Price latestBtcPrice = priceRepository.findLatestPriceBySymbol("BTCUSDT");

        assertNotNull(latestBtcPrice, "Latest price should not be null");
        assertAll("Latest BTC price verification",
                () -> assertEquals("BTCUSDT", latestBtcPrice.getSymbol()),
                () -> assertEquals(new BigDecimal("50100.00000000"), latestBtcPrice.getBidPrice()),
                () -> assertEquals(new BigDecimal("50300.00000000"), latestBtcPrice.getAskPrice()),
                () -> assertEquals("HUOBI", latestBtcPrice.getExchange()),
                () -> assertEquals(baseTime, latestBtcPrice.getTimestamp())
        );
    }

    @Test
    @DisplayName("Should find the most recent ETH price")
    void findLatestPriceBySymbol_ForETH_ShouldReturnMostRecentPrice() {
        Price latestEthPrice = priceRepository.findLatestPriceBySymbol("ETHUSDT");

        assertNotNull(latestEthPrice, "Latest ETH price should not be null");
        assertAll("Latest ETH price verification",
                () -> assertEquals("ETHUSDT", latestEthPrice.getSymbol()),
                () -> assertEquals(new BigDecimal("3000.00000000"), latestEthPrice.getBidPrice()),
                () -> assertEquals(new BigDecimal("3010.00000000"), latestEthPrice.getAskPrice()),
                () -> assertEquals("HUOBI", latestEthPrice.getExchange()),
                () -> assertEquals(baseTime.minusMinutes(1), latestEthPrice.getTimestamp())
        );
    }

    @Test
    @DisplayName("Should return null for non-existent symbol")
    void findLatestPriceBySymbol_ForNonExistentSymbol_ShouldReturnNull() {
        Price nonExistentPrice = priceRepository.findLatestPriceBySymbol("XRPUSDT");

        assertNull(nonExistentPrice, "Price for non-existent symbol should be null");
    }

    @Test
    @DisplayName("Should handle case-sensitive symbol matching")
    void findLatestPriceBySymbol_ShouldBeCaseSensitive() {
        Price upperCasePrice = priceRepository.findLatestPriceBySymbol("BTCUSDT");
        Price lowerCasePrice = priceRepository.findLatestPriceBySymbol("btcusdt");

        assertNotNull(upperCasePrice, "Upper case symbol should return a price");
        assertNull(lowerCasePrice, "Lower case symbol should not return a price");
    }
}