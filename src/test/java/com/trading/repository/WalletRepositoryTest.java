package com.trading.repository;

import com.trading.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class WalletRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WalletRepository walletRepository;

    @BeforeEach
    void setUp() {
        entityManager.clear();
        walletRepository.deleteAll();

        Wallet btcWallet = createWallet(1L, "BTC", 1.5);
        Wallet ethWallet = createWallet(1L, "ETH", 10.0);
        Wallet anotherUserWallet = createWallet(2L, "BTC", 0.5);

        entityManager.persist(btcWallet);
        entityManager.persist(ethWallet);
        entityManager.persist(anotherUserWallet);
        entityManager.flush();
    }

    private Wallet createWallet(Long userId, String currency, Double balance) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setCurrency(currency);
        wallet.setBalance(BigDecimal.valueOf(balance));
        return wallet;
    }

    @Test
    void findByUserId_WhenUserHasMultipleWallets_ShouldReturnAllWallets() {
        List<Wallet> foundWallets = walletRepository.findByUserId(1L);

        assertEquals(2, foundWallets.size());
        assertTrue(foundWallets.stream()
                .anyMatch(wallet -> wallet.getCurrency().equals("BTC") &&
                        wallet.getBalance().equals(BigDecimal.valueOf(1.5))));
        assertTrue(foundWallets.stream()
                .anyMatch(wallet -> wallet.getCurrency().equals("ETH") &&
                        wallet.getBalance().equals(BigDecimal.valueOf(10.0))));
    }

    @Test
    void findByUserId_WhenUserHasNoWallets_ShouldReturnEmptyList() {
        List<Wallet> foundWallets = walletRepository.findByUserId(999L);

        assertTrue(foundWallets.isEmpty());
    }

    @Test
    void findByUserIdAndCurrency_WhenWalletExists_ShouldReturnWallet() {
        Wallet foundWallet = walletRepository.findByUserIdAndCurrency(1L, "BTC");

        assertNotNull(foundWallet);
        assertEquals("BTC", foundWallet.getCurrency());
        assertEquals(BigDecimal.valueOf(1.5), foundWallet.getBalance());
        assertEquals(1L, foundWallet.getUserId());
    }

    @Test
    void findByUserIdAndCurrency_WhenWalletDoesNotExist_ShouldReturnNull() {
        Wallet foundWallet = walletRepository.findByUserIdAndCurrency(1L, "XRP");

        assertNull(foundWallet);
    }

    @Test
    void findByUserIdAndCurrency_WhenUserDoesNotExist_ShouldReturnNull() {
        Wallet foundWallet = walletRepository.findByUserIdAndCurrency(999L, "BTC");

        assertNull(foundWallet);
    }
}