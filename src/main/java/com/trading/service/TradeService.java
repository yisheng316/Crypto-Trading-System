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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeService {
    private final TradeRepository tradeRepository;
    private final PriceService priceService;
    private final WalletService walletService;

    public List<Trade> getUserTrades(Long userId) {
        return tradeRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    @Transactional
    public TradeResponse executeTrade(Long userId, TradeRequest request) {
        validateTradeRequest(request);

        Price latestPrice = priceService.getLatestPrice(request.getSymbol())
                .orElseThrow(() -> new PriceNotFoundException("No price available for " + request.getSymbol()));

        BigDecimal tradePrice = request.getType().equals("BUY") ?
                latestPrice.getAskPrice() : latestPrice.getBidPrice();

        BigDecimal total = tradePrice.multiply(request.getQuantity());

        if (request.getType().equals("BUY")) {
            executeBuyTrade(userId, request.getSymbol(), total, request.getQuantity());
        } else {
            executeSellTrade(userId, request.getSymbol(), total, request.getQuantity());
        }

        Trade trade = createTrade(userId, request, tradePrice, total);
        return mapTradeToResponse(tradeRepository.save(trade));
    }

    private void validateTradeRequest(TradeRequest request) {
        if (request.getSymbol() == null || request.getQuantity() == null || request.getType() == null) {
            throw new InvalidTradeException("Symbol, quantity, and type are required");
        }

        if (!request.getSymbol().equals("BTCUSDT") && !request.getSymbol().equals("ETHUSDT")) {
            throw new InvalidTradeException("Only BTCUSDT and ETHUSDT pairs are supported");
        }

        if (!request.getType().equals("BUY") && !request.getType().equals("SELL")) {
            throw new InvalidTradeException("Trade type must be either BUY or SELL");
        }

        if (request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTradeException("Quantity must be greater than 0");
        }
    }

    private void executeBuyTrade(Long userId, String symbol, BigDecimal total, BigDecimal quantity) {
        Wallet usdtWallet = walletService.getWalletByCurrency(userId, "USDT")
                .orElseThrow(() -> new RuntimeException("USDT wallet not found"));

        if (usdtWallet.getBalance().compareTo(total) < 0) {
            throw new InsufficientBalanceException("Insufficient USDT balance");
        }

        String cryptoCurrency = symbol.replace("USDT", "");
        walletService.updateBalance(userId, "USDT", total.negate());
        walletService.updateBalance(userId, cryptoCurrency, quantity);
    }

    private void executeSellTrade(Long userId, String symbol, BigDecimal total, BigDecimal quantity) {
        String cryptoCurrency = symbol.replace("USDT", "");
        Wallet cryptoWallet = walletService.getWalletByCurrency(userId, cryptoCurrency)
                .orElseThrow(() -> new RuntimeException(cryptoCurrency + " wallet not found"));

        if (cryptoWallet.getBalance().compareTo(quantity) < 0) {
            throw new InsufficientBalanceException("Insufficient " + cryptoCurrency + " balance");
        }

        walletService.updateBalance(userId, cryptoCurrency, quantity.negate());
        walletService.updateBalance(userId, "USDT", total);
    }

    private Trade createTrade(Long userId, TradeRequest request, BigDecimal price, BigDecimal total) {
        Trade trade = new Trade();
        trade.setUserId(userId);
        trade.setSymbol(request.getSymbol());
        trade.setType(request.getType());
        trade.setPrice(price);
        trade.setQuantity(request.getQuantity());
        trade.setTotal(total);
        trade.setTimestamp(LocalDateTime.now());
        return trade;
    }

    private TradeResponse mapTradeToResponse(Trade trade) {
        TradeResponse response = new TradeResponse();
        response.setTradeId(trade.getId());
        response.setSymbol(trade.getSymbol());
        response.setType(trade.getType());
        response.setPrice(trade.getPrice());
        response.setQuantity(trade.getQuantity());
        response.setTotal(trade.getTotal());
        response.setTimestamp(trade.getTimestamp());
        return response;
    }
}
