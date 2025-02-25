package com.example.personalfinancetracker.controller;

import com.example.personalfinancetracker.dto.PagedTransactionResponseDTO;
import com.example.personalfinancetracker.dto.TransactionRequestDTO;
import com.example.personalfinancetracker.dto.TransactionResponseDTO;
import com.example.personalfinancetracker.dto.TransactionSearchCriteriaDTO;
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
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

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

    @GetMapping("/balance/{accountName}")
    public ResponseEntity<BigDecimal> getBalance(
            @PathVariable String accountName,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
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

    @GetMapping
    public ResponseEntity<PagedTransactionResponseDTO> searchTransactions(
            @Valid @ModelAttribute TransactionSearchCriteriaDTO searchCriteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PagedTransactionResponseDTO response = transactionService.searchTransactions(
                searchCriteria, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
