package com.trading.service;

import com.trading.exception.InsufficientBalanceException;
import com.trading.model.Wallet;
import com.trading.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    private final Long USER_ID = 1L;
    private final String USDT = "USDT";
    private final String BTC = "BTC";
    private final BigDecimal INITIAL_BALANCE = new BigDecimal("50000.00");

    @Nested
    @DisplayName("getUserWallets Tests")
    class GetUserWalletsTests {

        @Test
        @DisplayName("Should return all wallets for a user")
        void shouldReturnAllWalletsForUser() {
            List<Wallet> expectedWallets = Arrays.asList(
                    new Wallet(1L, USER_ID, USDT, INITIAL_BALANCE),
                    new Wallet(2L, USER_ID, BTC, BigDecimal.ZERO)
            );
            when(walletRepository.findByUserId(USER_ID)).thenReturn(expectedWallets);

            List<Wallet> actualWallets = walletService.getUserWallets(USER_ID);

            assertAll(
                    () -> assertEquals(2, actualWallets.size(), "Should return correct number of wallets"),
                    () -> assertEquals(USDT, actualWallets.get(0).getCurrency(), "First wallet should be USDT"),
                    () -> assertEquals(BTC, actualWallets.get(1).getCurrency(), "Second wallet should be BTC"),
                    () -> assertEquals(INITIAL_BALANCE, actualWallets.get(0).getBalance(), "USDT balance should match")
            );
            verify(walletRepository).findByUserId(USER_ID);
        }

        @Test
        @DisplayName("Should return empty list when user has no wallets")
        void shouldReturnEmptyListWhenNoWallets() {
            when(walletRepository.findByUserId(USER_ID)).thenReturn(Collections.emptyList());

            List<Wallet> actualWallets = walletService.getUserWallets(USER_ID);

            assertTrue(actualWallets.isEmpty(), "Should return empty list when user has no wallets");
            verify(walletRepository).findByUserId(USER_ID);
        }
    }

    @Nested
    @DisplayName("updateBalance Tests")
    class UpdateBalanceTests {

        private Wallet existingWallet;

        @BeforeEach
        void setUp() {
            existingWallet = new Wallet(1L, USER_ID, USDT, INITIAL_BALANCE);
        }

        @Test
        @DisplayName("Should add balance to existing wallet")
        void shouldAddBalanceToExistingWallet() {
            BigDecimal addAmount = new BigDecimal("1000.00");
            BigDecimal expectedBalance = INITIAL_BALANCE.add(addAmount);
            when(walletRepository.findByUserIdAndCurrency(USER_ID, USDT)).thenReturn(existingWallet);
            when(walletRepository.save(any(Wallet.class))).thenReturn(existingWallet);

            walletService.updateBalance(USER_ID, USDT, addAmount);

            assertEquals(expectedBalance, existingWallet.getBalance(),
                    "Balance should be updated correctly");
            verify(walletRepository).save(existingWallet);
        }

        @Test
        @DisplayName("Should create new wallet if it doesn't exist")
        void shouldCreateNewWalletIfNotExists() {
            String newCurrency = "ETH";
            BigDecimal initialAmount = new BigDecimal("1.0");
            when(walletRepository.findByUserIdAndCurrency(USER_ID, newCurrency)).thenReturn(null);

            walletService.updateBalance(USER_ID, newCurrency, initialAmount);

            verify(walletRepository).save(argThat(wallet ->
                    wallet.getUserId().equals(USER_ID) &&
                            wallet.getCurrency().equals(newCurrency) &&
                            wallet.getBalance().equals(initialAmount)
            ));
        }

        @Test
        @DisplayName("Should throw InsufficientBalanceException when balance would become negative")
        void shouldThrowExceptionOnNegativeBalance() {
            BigDecimal deductAmount = new BigDecimal("-60000.00");
            when(walletRepository.findByUserIdAndCurrency(USER_ID, USDT)).thenReturn(existingWallet);

            InsufficientBalanceException exception = assertThrows(
                    InsufficientBalanceException.class,
                    () -> walletService.updateBalance(USER_ID, USDT, deductAmount),
                    "Should throw InsufficientBalanceException when balance would become negative"
            );

            assertEquals("Insufficient balance in USDT wallet", exception.getMessage());
            verify(walletRepository, never()).save(any(Wallet.class));
        }
    }

    @Nested
    @DisplayName("getWalletByCurrency Tests")
    class GetWalletByCurrencyTests {

        @Test
        @DisplayName("Should return wallet when it exists")
        void shouldReturnWalletWhenExists() {
            Wallet expectedWallet = new Wallet(1L, USER_ID, USDT, INITIAL_BALANCE);
            when(walletRepository.findByUserIdAndCurrency(USER_ID, USDT)).thenReturn(expectedWallet);

            Optional<Wallet> actualWallet = walletService.getWalletByCurrency(USER_ID, USDT);

            assertTrue(actualWallet.isPresent(), "Wallet should be present");
            assertEquals(expectedWallet, actualWallet.get(), "Should return correct wallet");
        }

        @Test
        @DisplayName("Should return empty Optional when wallet doesn't exist")
        void shouldReturnEmptyOptionalWhenWalletNotExists() {
            when(walletRepository.findByUserIdAndCurrency(USER_ID, USDT)).thenReturn(null);

            Optional<Wallet> actualWallet = walletService.getWalletByCurrency(USER_ID, USDT);

            assertTrue(actualWallet.isEmpty(), "Should return empty Optional when wallet doesn't exist");
        }
    }
}