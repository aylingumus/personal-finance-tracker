package com.example.personalfinancetracker.controller;

import com.example.personalfinancetracker.dto.PagedTransactionResponseDTO;
import com.example.personalfinancetracker.dto.TransactionRequestDTO;
import com.example.personalfinancetracker.dto.TransactionResponseDTO;
import com.example.personalfinancetracker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    // TO-DO: consistent, robust app - relational db and transaction management - add to readme
    // TO-DO: now currency is global, but it can be extended in the future (add in readme)
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> addTransaction(@Valid @RequestBody TransactionRequestDTO requestDTO) {
        TransactionResponseDTO responseDTO = transactionService.addTransaction(requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/account/{accountName}")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByAccount(@PathVariable String accountName) {
        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByAccount(accountName);
        return ResponseEntity.ok(transactions);
    }

    // TO-DO: Think about adding a cache layer for calculating balance part - for not calculating each time
    @GetMapping("/balance/{accountName}")
    public ResponseEntity<BigDecimal> getBalance(
            @PathVariable String accountName,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // in cache, can be stored: key: "account name + date" as (Aylin_24022025), value: balance (10.0)
        LocalDate givenDate = date != null ? date : LocalDate.now();
        BigDecimal balance = transactionService.calculateBalance(accountName, givenDate);
        return ResponseEntity.ok(balance);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequestDTO requestDTO) {
        TransactionResponseDTO updatedTransaction = transactionService.updateTransaction(id, requestDTO);
        return ResponseEntity.ok(updatedTransaction);
    }

    // TO-DO: Consider adding filtration feature based on days, weeks, months and years
    // TO-DO: Create a FilterRequestDTO - @RequestParam object - it is mapping automatically
    @GetMapping
    public ResponseEntity<PagedTransactionResponseDTO> searchTransactions(
            @RequestParam(required = false) String accountName,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PagedTransactionResponseDTO response = transactionService.getFilteredTransactions(
                accountName, minAmount, maxAmount, fromDate, toDate, category, description, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }
}
