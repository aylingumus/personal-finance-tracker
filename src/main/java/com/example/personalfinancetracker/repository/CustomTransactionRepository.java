package com.example.personalfinancetracker.repository;

import com.example.personalfinancetracker.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CustomTransactionRepository {
    Page<Transaction> findTransactionsByCriteria(String accountName,
                                                 BigDecimal minAmount,
                                                 BigDecimal maxAmount,
                                                 LocalDate fromDate,
                                                 LocalDate toDate,
                                                 String category,
                                                 String description,
                                                 Pageable pageable);

    BigDecimal calculateTotalBalanceByCriteria(String accountName,
                                               BigDecimal minAmount,
                                               BigDecimal maxAmount,
                                               LocalDate fromDate,
                                               LocalDate toDate,
                                               String category,
                                               String description);
}
