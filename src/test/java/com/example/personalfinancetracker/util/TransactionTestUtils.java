package com.example.personalfinancetracker.util;

import com.example.personalfinancetracker.domain.Transaction;
import com.example.personalfinancetracker.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionTestUtils {

    public static Transaction createAndSaveTransaction(TransactionRepository repository,
                                                       String accountName,
                                                       BigDecimal amount,
                                                       String category,
                                                       String description,
                                                       LocalDateTime createdAt) {
        Transaction tx = new Transaction();
        tx.setAccountName(accountName);
        tx.setAmount(amount);
        tx.setCreatedAt(createdAt != null ? createdAt : LocalDateTime.now());
        tx.setCategory(category);
        tx.setDescription(description);
        return repository.saveAndFlush(tx);
    }
}

