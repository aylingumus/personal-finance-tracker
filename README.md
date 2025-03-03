# Personal Finance Tracker

A robust REST API application for managing personal finances, allowing users to track transactions across different accounts.

## Features

- Create, update, delete and retrieve financial transactions
- Search transactions with multiple filtering options, ordering, and pagination
- **Dynamic total balance:** The search response includes the net balance based on applied filters
- **Current net balance:** A separate endpoint provides the net balance for an account on a given date
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

- `GET /api/v1/transactions` - Get all transactions with filtering options
- `GET /api/v1/transactions/account/{accountName}` - Get all transactions for an account
- `GET /api/v1/transactions/balance/{accountName}` - Get current balance for an account
- `GET /api/v1/transactions/balance/{accountName}?date=2025-02-24` - Get balance for an account at a specific date
- `PUT /api/v1/transactions/{id}` - Update a transaction
- `POST /api/v1/transactions` - Create a new transaction
- `DELETE /api/v1/transactions/{id}` - Delete a transaction

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
- **Reporting Features**: The reporting capabilities can be added. I can implement monthly, daily, and yearly reports to provide better financial statistics.
- **Categorized Financial Reports**: Financial reports can be grouped by expenses and incomes. I can add this for a clearer financial overview.
