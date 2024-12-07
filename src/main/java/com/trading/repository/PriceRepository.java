package com.trading.repository;

import com.trading.model.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PriceRepository extends JpaRepository<Price, Long> {
    @Query("SELECT p FROM Price p WHERE p.symbol = ?1 ORDER BY p.timestamp DESC LIMIT 1")
    Price findLatestPriceBySymbol(String symbol);
}
