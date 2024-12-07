package com.trading.service;

import com.trading.dto.BinanceResponse;
import com.trading.dto.HuobiPrice;
import com.trading.dto.HuobiWrapper;
import com.trading.model.Price;
import com.trading.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PriceService {
    private final PriceRepository priceRepository;
    private final RestTemplate restTemplate;
    public static final List<String> TRADING_PAIRS = Arrays.asList("BTCUSDT", "ETHUSDT");

    @Value("${binance.api.url:https://api.binance.com}")
    private String binanceBaseUrl;

    @Value("${huobi.api.url:https://api.huobi.pro}")
    private String huobiBaseUrl;

    public Optional<Price> getLatestPrice(String symbol) {
        try {
            return Optional.ofNullable(priceRepository.findLatestPriceBySymbol(symbol));
        } catch (Exception e) {
            log.error("Error fetching latest price for symbol {}: {}", symbol, e.getMessage());
            return Optional.empty();
        }
    }

    @Scheduled(fixedRate = 10000)
    public void fetchAndSavePrices() {
        try {
            // Fetch from Binance with specific symbols and retry
            BinanceResponse[] binanceData = fetchBinancePrices();

            // Fetch from Huobi
            HuobiWrapper huobiData = fetchHuobiPrices();

            // Process prices if we have data from at least one source
            if (binanceData != null || (huobiData != null && huobiData.getData() != null)) {
                processTradingPairs(binanceData, huobiData);
            } else {
                log.error("Could not fetch prices from any exchange");
            }
        } catch (Exception e) {
            log.error("Unexpected error in price fetching scheduler: ", e);
        }
    }

    private BinanceResponse[] fetchBinancePrices() {
        try {
            String binanceUrl = binanceBaseUrl + "/api/v3/ticker/bookTicker";

            log.debug("Fetching all symbols from Binance URL: {}", binanceUrl);
            ResponseEntity<BinanceResponse[]> binanceResponse =
                    restTemplate.getForEntity(binanceUrl, BinanceResponse[].class);

            if (binanceResponse.getStatusCode().is2xxSuccessful() && binanceResponse.getBody() != null) {
                BinanceResponse[] allData = binanceResponse.getBody();
                BinanceResponse[] filteredData = Arrays.stream(allData)
                        .filter(price -> TRADING_PAIRS.contains(price.getSymbol()))
                        .toArray(BinanceResponse[]::new);

                log.info("Successfully fetched Binance prices. Total: {}, Filtered: {}",
                        allData.length, filteredData.length);

                Arrays.stream(filteredData).forEach(price ->
                        log.debug("Binance {} - Bid: {}, Ask: {}",
                                price.getSymbol(), price.getBidPrice(), price.getAskPrice())
                );
                return filteredData;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch Binance prices: {}. Will continue with Huobi only.",
                    e.getMessage());
        }
        return null;
    }

    private HuobiWrapper fetchHuobiPrices() {
        try {
            String huobiUrl = huobiBaseUrl + "/market/tickers";
            ResponseEntity<HuobiWrapper> huobiResponse =
                    restTemplate.getForEntity(huobiUrl, HuobiWrapper.class);

            if (huobiResponse.getStatusCode().is2xxSuccessful() &&
                    huobiResponse.getBody() != null &&
                    huobiResponse.getBody().getData() != null) {
                HuobiWrapper data = huobiResponse.getBody();
                log.info("Successfully fetched Huobi prices for {} symbols",
                        data.getData().size());

                data.getData().stream()
                        .filter(price -> TRADING_PAIRS.contains(price.getSymbol().toUpperCase()))
                        .forEach(price ->
                                log.debug("Huobi {} - Bid: {}, Ask: {}",
                                        price.getSymbol().toUpperCase(), price.getBid(), price.getAsk())
                        );
                return data;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch Huobi prices: {}", e.getMessage());
        }
        return null;
    }

    private void processTradingPairs(BinanceResponse[] binanceData, HuobiWrapper huobiData) {
        for (String pair : TRADING_PAIRS) {
            try {
                BinanceResponse binancePrice = null;
                HuobiPrice huobiPrice = null;

                // Get Binance price if available
                if (binanceData != null) {
                    binancePrice = Arrays.stream(binanceData)
                            .filter(p -> p.getSymbol().equals(pair))
                            .findFirst()
                            .orElse(null);
                }

                // Get Huobi price if available
                if (huobiData != null && huobiData.getData() != null) {
                    String huobiSymbol = pair.toLowerCase();
                    huobiPrice = huobiData.getData().stream()
                            .filter(p -> p.getSymbol().equals(huobiSymbol))
                            .findFirst()
                            .orElse(null);
                }

                if (binancePrice != null || huobiPrice != null) {
                    savePrice(pair, binancePrice, huobiPrice);
                } else {
                    log.warn("No price data available for {}", pair);
                }
            } catch (Exception e) {
                log.error("Error processing trading pair {}: {}", pair, e.getMessage());
            }
        }
    }

    private void savePrice(String symbol, BinanceResponse binance, HuobiPrice huobi) {
        Price price = new Price();
        price.setSymbol(symbol);
        price.setTimestamp(LocalDateTime.now());

        if (binance != null && huobi != null) {
            // Compare prices from both exchanges
            BigDecimal binanceBid = new BigDecimal(binance.getBidPrice());
            BigDecimal binanceAsk = new BigDecimal(binance.getAskPrice());
            BigDecimal huobiBid = huobi.getBid();
            BigDecimal huobiAsk = huobi.getAsk();

            price.setBidPrice(binanceBid.compareTo(huobiBid) > 0 ? binanceBid : huobiBid);
            price.setAskPrice(binanceAsk.compareTo(huobiAsk) < 0 ? binanceAsk : huobiAsk);
            price.setExchange(determineExchange(price, binanceBid, binanceAsk, huobiBid, huobiAsk));

            log.debug("Compared prices for {}: Binance({}/{}) Huobi({}/{})",
                    symbol, binanceBid, binanceAsk, huobiBid, huobiAsk);
        } else if (binance != null) {
            price.setBidPrice(new BigDecimal(binance.getBidPrice()));
            price.setAskPrice(new BigDecimal(binance.getAskPrice()));
            price.setExchange("BINANCE");
        } else {
            price.setBidPrice(huobi.getBid());
            price.setAskPrice(huobi.getAsk());
            price.setExchange("HUOBI");
        }

        Price savedPrice = priceRepository.save(price);
        log.info("Saved price for {}: Bid={}, Ask={}, Exchange={}",
                savedPrice.getSymbol(),
                savedPrice.getBidPrice(),
                savedPrice.getAskPrice(),
                savedPrice.getExchange());
    }

    private String determineExchange(Price price, BigDecimal binanceBid, BigDecimal binanceAsk,
                                     BigDecimal huobiBid, BigDecimal huobiAsk) {
        boolean isBinanceBest = price.getBidPrice().equals(binanceBid) && price.getAskPrice().equals(binanceAsk);
        boolean isHuobiBest = price.getBidPrice().equals(huobiBid) && price.getAskPrice().equals(huobiAsk);

        if (isBinanceBest) return "BINANCE";
        if (isHuobiBest) return "HUOBI";
        return "MIXED";
    }
}