package com.trading.controller;

import com.trading.model.Wallet;
import com.trading.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletController walletController;

    private List<Wallet> testWallets;

    @BeforeEach
    void setUp() {
        Wallet wallet1 = new Wallet();
        wallet1.setId(1L);

        Wallet wallet2 = new Wallet();
        wallet2.setId(2L);

        testWallets = Arrays.asList(wallet1, wallet2);
    }

    @Test
    void getUserWallets_ShouldReturnWalletsList() {
        when(walletService.getUserWallets(1L)).thenReturn(testWallets);

        ResponseEntity<List<Wallet>> response = walletController.getUserWallets();

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(testWallets, response.getBody()),
                () -> assertEquals(2, response.getBody().size())
        );

        verify(walletService, times(1)).getUserWallets(1L);
    }

    @Test
    void getUserWallets_WhenNoWallets_ShouldReturnEmptyList() {
        when(walletService.getUserWallets(1L)).thenReturn(List.of());

        ResponseEntity<List<Wallet>> response = walletController.getUserWallets();

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertTrue(response.getBody().isEmpty())
        );

        verify(walletService, times(1)).getUserWallets(1L);
    }
}