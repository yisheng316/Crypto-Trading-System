package com.trading.repository;

import com.trading.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    List<Wallet> findByUserId(Long userId);
    Wallet findByUserIdAndCurrency(Long userId, String currency);
}
