package com.example.personalfinancetracker.controller;

import com.example.personalfinancetracker.domain.Transaction;
import com.example.personalfinancetracker.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Transaction> addTransaction(@RequestBody Transaction transaction) {
        Transaction savedTransaction = transactionService.addTransaction(transaction);
        return ResponseEntity.ok(savedTransaction);
    }

    // TO-DO: Consider adding pagination to retrieve all transactions
    // TO-DO: Add an ability ability to apply filters, ordering and see the sum of all resulting spendings/incomes
    // TO-DO: Consider adding filtration feature based on days, weeks, months and years
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/account/{accountName}")
    public ResponseEntity<List<Transaction>> getTransactionsByAccount(@PathVariable String accountName) {
        List<Transaction> transactions = transactionService.getTransactionsByAccount(accountName);
        return ResponseEntity.ok(transactions);
    }

    // TO-DO: Think about adding a cache layer for calculating balance part
    @GetMapping("/balance/{accountName}")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String accountName) {
        BigDecimal balance = transactionService.calculateBalance(accountName);
        return ResponseEntity.ok(balance);
    }

    // TO-DO: Add an endpoint for modifying an existing spending/income
}
