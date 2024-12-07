package com.trading.service;

import com.trading.exception.InsufficientBalanceException;
import com.trading.model.Wallet;
import com.trading.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;

    public List<Wallet> getUserWallets(Long userId) {
        return walletRepository.findByUserId(userId);
    }

    public Optional<Wallet> getWalletByCurrency(Long userId, String currency) {
        return Optional.ofNullable(walletRepository.findByUserIdAndCurrency(userId, currency));
    }

    @Transactional
    public void updateBalance(Long userId, String currency, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserIdAndCurrency(userId, currency);
        if (wallet == null) {
            wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setCurrency(currency);
            wallet.setBalance(amount);
        } else {
            wallet.setBalance(wallet.getBalance().add(amount));
        }

        if (wallet.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in " + currency + " wallet");
        }

        walletRepository.save(wallet);
    }
}
