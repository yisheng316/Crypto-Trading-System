package com.trading.exception;

public class ExchangeConnectionException extends RuntimeException {
    public ExchangeConnectionException(String message) {
        super(message);
    }

    public ExchangeConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
