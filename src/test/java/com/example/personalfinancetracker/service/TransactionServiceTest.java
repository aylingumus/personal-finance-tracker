package com.example.personalfinancetracker.service;

import com.example.personalfinancetracker.domain.Transaction;
import com.example.personalfinancetracker.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    @Test
    void testCalculateBalance() {
        Transaction t1 = new Transaction();
        t1.setAccountName("Aylin");
        t1.setAmount(BigDecimal.valueOf(10));
        t1.setTimestamp(LocalDateTime.now());
        t1.setCategory("Income");
        t1.setDescription("Salary");
        transactionService.addTransaction(t1);

        Transaction t2 = new Transaction();
        t2.setAccountName("Aylin");
        t2.setAmount(BigDecimal.valueOf(-3));
        t2.setTimestamp(LocalDateTime.now());
        t2.setCategory("Expense");
        t2.setDescription("Lunch");
        transactionService.addTransaction(t2);

        BigDecimal balance = transactionService.calculateBalance("Aylin");
        assertThat(balance).isEqualByComparingTo(BigDecimal.valueOf(7));
    }
}

