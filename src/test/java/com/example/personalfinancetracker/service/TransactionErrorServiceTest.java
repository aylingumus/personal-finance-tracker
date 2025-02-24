package com.example.personalfinancetracker.service;

import com.example.personalfinancetracker.domain.Transaction;
import com.example.personalfinancetracker.dto.TransactionRequestDTO;
import com.example.personalfinancetracker.mapper.TransactionMapper;
import com.example.personalfinancetracker.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionErrorServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        TransactionRequestDTO requestDTO = new TransactionRequestDTO();
        requestDTO.setAccountName("TestAccount");
        requestDTO.setAmount(new BigDecimal("100.00"));
        requestDTO.setCategory("Food");
        requestDTO.setDescription("Lunch");

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAccountName("TestAccount");
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setCategory("Food");
        transaction.setDescription("Lunch");
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setVersion(0L);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonexistentTransaction() {
        when(transactionRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        TransactionRequestDTO updateRequest = new TransactionRequestDTO();
        updateRequest.setAccountName("TestAccount");
        updateRequest.setAmount(new BigDecimal("150.00"));
        updateRequest.setCategory("Food");
        updateRequest.setDescription("Updated Lunch");

        assertThrows(RuntimeException.class, () ->
                transactionService.updateTransaction(999L, updateRequest)
        );
    }

    // TO-DO: Add balance without given date - null
}
