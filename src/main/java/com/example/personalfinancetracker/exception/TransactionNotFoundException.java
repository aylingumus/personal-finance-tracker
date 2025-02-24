package com.example.personalfinancetracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(Long id) {
        super("Transaction not found for id: " + id);
    }

    public TransactionNotFoundException(String accountName) {
        super("No transactions found for account: " + accountName);
    }
}
