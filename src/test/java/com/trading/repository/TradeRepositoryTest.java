package com.trading.repository;

import com.trading.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ExtendWith(SpringExtension.class)
class TradeRepositoryTest {

    @Autowired
    private TradeRepository tradeRepository;

    private Trade trade1;
    private Trade trade2;
    private Trade trade3;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        trade1 = new Trade();
        trade1.setId(1L);
        trade1.setUserId(1L);
        trade1.setSymbol("BTCUSDT");
        trade1.setQuantity(new BigDecimal("0.5"));
        trade1.setPrice(new BigDecimal("50000.00"));
        trade1.setTotal(new BigDecimal("25000.00"));
        trade1.setTimestamp(now.minusDays(1));
        trade1.setType("BUY");

        trade2 = new Trade();
        trade2.setId(2L);
        trade2.setUserId(1L);
        trade2.setSymbol("ETHUSDT");
        trade2.setQuantity(new BigDecimal("2.0"));
        trade2.setPrice(new BigDecimal("3000.00"));
        trade2.setTotal(new BigDecimal("6000.00"));
        trade2.setTimestamp(now);
        trade2.setType("SELL");

        trade3 = new Trade();
        trade3.setId(3L);
        trade3.setUserId(2L);
        trade3.setSymbol("XRPUSDT");
        trade3.setQuantity(new BigDecimal("10.0"));
        trade3.setPrice(new BigDecimal("1.00"));
        trade3.setTotal(new BigDecimal("10.00"));
        trade3.setTimestamp(now.minusWeeks(1));
        trade3.setType("BUY");
    }

    @Test
    @DisplayName("Should return user's trading history in descending order")
    void findByUserIdOrderByTimestampDesc() {
        tradeRepository.save(trade1);
        tradeRepository.save(trade2);
        tradeRepository.save(trade3);

        List<Trade> userTrades = tradeRepository.findByUserIdOrderByTimestampDesc(1L);

        assertThat(userTrades)
                .hasSize(2)
                .extracting(Trade::getSymbol)
                .containsExactly("ETHUSDT", "BTCUSDT");
        assertThat(userTrades)
                .extracting(Trade::getTimestamp)
                .isSortedAccordingTo((t1, t2) -> t2.compareTo(t1));
    }

    @Test
    @DisplayName("Should return empty list when no trades found")
    void findByUserIdOrderByTimestampDesc_WhenNoTrades() {
        List<Trade> userTrades = tradeRepository.findByUserIdOrderByTimestampDesc(100L);

        assertThat(userTrades).isEmpty();
    }
}