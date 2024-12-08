# Crypto Trading System

A Spring Boot-based cryptocurrency trading system that aggregates prices from multiple exchanges (Binance and Huobi) and enables users to execute trades using the best available prices. The system maintains user wallets and provides a complete trading history.

## Features

- Real-time price aggregation from multiple cryptocurrency exchanges
- Best price selection for trading execution
- User wallet management with support for multiple cryptocurrencies
- Trading history tracking
- RESTful API interface
- Support for multiple trading pairs (BTCUSDT, ETHUSDT)
- Automatic price updates every 10 seconds

## Technology Stack

- Java 17
- Spring Boot 3.2.3
- H2 Database (in-memory)
- Maven
- Spring Data JPA
- Swagger/OpenAPI Documentation
- JUnit 5 for testing

## Getting Started

### Prerequisites

- JDK 17 or higher
- Maven 3.6 or higher
- Your favorite IDE (IntelliJ IDEA recommended)

### Installation

1. Navigate to the project directory:
```bash
  git clone https://github.com/yourusername/crypto-trading-system.git
 ```
2. Navigate to the project directory:
```bash
  cd crypto-trading-system
```
3. Build the project
```bash
  mvn clean install
```
4. Run the application:
```bash
  mvn spring-boot:run
```
The application will start on http://localhost:8080

## Database Configuration
The application uses an H2 in-memory database. You can access the H2 console at:

- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:tradingdb
- Username: sa
- Password: (leave empty)

## API Documentation
The API documentation is available through Swagger UI at http://localhost:8080/swagger-ui.html


## Key Endpoints
### Price API

- GET /api/prices/latest/{symbol} - Get latest price for a trading pair
- GET /api/prices/latest - Get all latest prices

### Wallet API

- GET /api/wallets - Get user's wallet balances

### Trade API

- POST /api/trades - Execute a trade
- GET /api/trades - Get trading history

## Sample API Requests
### Execute a Trade
```bash
  POST "/api/trades"  
```
```json
{
    "symbol": "BTCUSDT",
    "type": "BUY",
    "quantity": 0.1
}
```
### Get Wallet Balance
```bash
  GET /api/wallets
```

