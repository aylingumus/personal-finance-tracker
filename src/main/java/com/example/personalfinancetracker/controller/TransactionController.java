package com.example.personalfinancetracker.controller;

import com.example.personalfinancetracker.dto.TransactionRequestDTO;
import com.example.personalfinancetracker.dto.TransactionResponseDTO;
import com.example.personalfinancetracker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> addTransaction(@Valid @RequestBody TransactionRequestDTO requestDTO) {
        TransactionResponseDTO responseDTO = transactionService.addTransaction(requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    // TO-DO: Consider adding pagination to retrieve all transactions
    // TO-DO: Add an ability ability to apply filters, ordering and see the sum of all resulting spendings/incomes
    // TO-DO: Consider adding filtration feature based on days, weeks, months and years
    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransactions() {
        List<TransactionResponseDTO> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/account/{accountName}")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByAccount(@PathVariable String accountName) {
        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByAccount(accountName);
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
