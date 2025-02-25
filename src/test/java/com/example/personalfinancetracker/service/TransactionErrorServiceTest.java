package com.example.personalfinancetracker.service;

import com.example.personalfinancetracker.domain.Transaction;
import com.example.personalfinancetracker.dto.TransactionRequestDTO;
import com.example.personalfinancetracker.dto.TransactionSearchCriteriaDTO;
import com.example.personalfinancetracker.exception.TransactionNotFoundException;
import com.example.personalfinancetracker.mapper.TransactionMapper;
import com.example.personalfinancetracker.repository.CustomTransactionRepository;
import com.example.personalfinancetracker.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionErrorServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomTransactionRepository customTransactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private TransactionRequestDTO requestDTO;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        requestDTO = new TransactionRequestDTO();
        requestDTO.setAccountName("Aylin");
        requestDTO.setAmount(new BigDecimal("100.00"));
        requestDTO.setCategory("Food");
        requestDTO.setDescription("Lunch");

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAccountName("Aylin");
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
        updateRequest.setAccountName("Aylin");
        updateRequest.setAmount(new BigDecimal("150.00"));
        updateRequest.setCategory("Food");
        updateRequest.setDescription("Updated Lunch");

        assertThrows(TransactionNotFoundException.class, () ->
                transactionService.updateTransaction(999L, updateRequest)
        );
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTransaction() {
        when(transactionRepository.existsById(999L)).thenReturn(false);

        assertThrows(TransactionNotFoundException.class, () ->
                transactionService.deleteTransaction(999L)
        );
    }

    @Test
    void shouldThrowExceptionWhenCalculatingBalanceForNonExistentAccount() {
        when(transactionRepository.findByAccountName("Alien"))
                .thenReturn(Collections.emptyList());

        assertThrows(TransactionNotFoundException.class, () ->
                transactionService.calculateBalance("Alien", LocalDate.now())
        );
    }

    @Test
    void shouldThrowExceptionWhenRepositorySaveThrowsException() {
        when(transactionMapper.toEntity(any(TransactionRequestDTO.class))).thenReturn(transaction);
        when(transactionRepository.save(any(Transaction.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () ->
                transactionService.addTransaction(requestDTO)
        );
    }

    @Test
    void shouldThrowExceptionWhenSearchingWithInvalidParameters() {
        when(customTransactionRepository.findTransactionsByCriteria(
                any(), any(), any(), any(), any(), any(), any(), any())
        ).thenThrow(new IllegalArgumentException("Invalid search parameters"));

        TransactionSearchCriteriaDTO criteria = new TransactionSearchCriteriaDTO();
        criteria.setMinAmount(new BigDecimal("100.00"));
        criteria.setMaxAmount(new BigDecimal("50.00"));

        assertThrows(IllegalArgumentException.class, () ->
                transactionService.searchTransactions(criteria, 0, 10, "createdAt", "asc")
        );
    }
}