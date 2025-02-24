# Personal Finance Tracker

A robust REST API application for managing personal finances, allowing users to track transactions across different accounts.

## Features

- Create, update, and retrieve financial transactions
- Calculate account balances at specific dates
- Search transactions with multiple filtering options
- Optimistic locking to prevent concurrent updates
- Comprehensive error handling
- Caching for improved performance

## Technology Stack

- **Java 21**
- **Spring Boot 3.x**
- **Spring Data JPA**
- **Hibernate Validator**
- **H2 Database** (for development)
- **Slf4j** for logging
- **JUnit 5 & Mockito** for testing

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6 or higher

### Running Locally

1. Build the application:
   ```
   mvn clean install
   ```

2. Run the application:
   ```
   mvn spring-boot:run
   ```

3. The API will be available at `http://localhost:8080`

## API Endpoints

### Transactions

- `GET /transactions` - Get all transactions with filtering options
- `GET /transactions/account/{accountName}` - Get all transactions for an account
- `GET /transactions/balance/{accountName}` - Get current balance for an account
- `GET /transactions/balance/{accountName}?date=2025-02-24` - Get balance for an account at a specific date
- `PUT /transactions/{id}` - Update a transaction
- `POST /transactions` - Create a new transaction

## Testing

Run the tests using:
```
mvn test
```

The application includes:
- Unit tests for services
- Integration tests for controllers

## Future Improvements

- **Relational DB & Transaction Management**: I'm currently using an in-memory H2 database for simplicity. In a production environment, I would switch to a robust relational database (e.g., PostgreSQL) and ensure consistent transaction management.
- **Extended Currency Support**: The application currently treats currency as global. I can extend it to support multiple currencies or conversions if needed.
