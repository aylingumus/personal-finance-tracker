package com.example.personalfinancetracker.service;

import com.example.personalfinancetracker.domain.Transaction;
import com.example.personalfinancetracker.dto.TransactionRequestDTO;
import com.example.personalfinancetracker.dto.TransactionResponseDTO;
import com.example.personalfinancetracker.dto.TransactionSearchCriteriaDTO;
import com.example.personalfinancetracker.mapper.TransactionMapper;
import com.example.personalfinancetracker.repository.CustomTransactionRepository;
import com.example.personalfinancetracker.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

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
    void shouldAddTransactionSuccessfully() {
        when(transactionMapper.toEntity(any(TransactionRequestDTO.class))).thenReturn(transaction);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        transactionService.addTransaction(requestDTO);

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldUpdateTransactionSuccessfully() {
        requestDTO.setAmount(new BigDecimal("150.00"));
        requestDTO.setCategory("Updated Food");
        requestDTO.setDescription("Updated Lunch");

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toDTO(any(Transaction.class))).thenReturn(new TransactionResponseDTO());

        TransactionResponseDTO result = transactionService.updateTransaction(1L, requestDTO);

        assertNotNull(result);
        verify(transactionRepository).findById(1L);
        verify(transactionRepository).save(any(Transaction.class));
        verify(transactionMapper).toDTO(any(Transaction.class));
    }

    @Test
    void shouldDeleteTransactionSuccessfully() {
        when(transactionRepository.existsById(1L)).thenReturn(true);

        transactionService.deleteTransaction(1L);

        verify(transactionRepository).deleteById(1L);
    }

    @Test
    void shouldCalculateBalanceForAccountOnGivenDate() {
        when(transactionRepository.findByAccountName(eq("Aylin")))
                .thenReturn(Collections.singletonList(transaction));
        when(transactionRepository.calculateBalanceForAccount(eq("Aylin"), any(LocalDate.class)))
                .thenReturn(new BigDecimal("300.00"));

        BigDecimal balance = transactionService.calculateBalance("Aylin", LocalDate.now());

        assertEquals(new BigDecimal("300.00"), balance);
        verify(transactionRepository).calculateBalanceForAccount(eq("Aylin"), any(LocalDate.class));
    }

    @Test
    void shouldReturnAllTransactionsWhenNoCriteriaProvided() {
        List<Transaction> allTransactions = Arrays.asList(transaction, createTransaction(new BigDecimal("200.00")));
        Page<Transaction> page = new PageImpl<>(allTransactions);

        when(customTransactionRepository.findTransactionsByCriteria(
                eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), any(PageRequest.class))
        ).thenReturn(page);

        when(customTransactionRepository.calculateTotalBalanceByCriteria(
                eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null))
        ).thenReturn(new BigDecimal("250.00"));

        TransactionSearchCriteriaDTO emptyCriteria = new TransactionSearchCriteriaDTO();

        var result = transactionService.searchTransactions(
                emptyCriteria, 0, 10, "createdAt", "desc"
        );

        assertNotNull(result);
        assertEquals(2, result.getTransactions().size());
        assertEquals(2, result.getTotalRecords());
        assertEquals(new BigDecimal("250.00"), result.getTotalBalance());
    }

    @Test
    void shouldReturnEmptyWhenNoTransactionsMatchCriteria() {
        Page<Transaction> emptyPage = new PageImpl<>(Collections.emptyList());

        when(customTransactionRepository.findTransactionsByCriteria(
                eq("Alien"), any(), any(), any(), any(), any(), any(), any(PageRequest.class))
        ).thenReturn(emptyPage);

        when(customTransactionRepository.calculateTotalBalanceByCriteria(
                eq("Alien"), any(), any(), any(), any(), any(), any())
        ).thenReturn(BigDecimal.ZERO);

        TransactionSearchCriteriaDTO criteria = new TransactionSearchCriteriaDTO();
        criteria.setAccountName("Alien");

        var result = transactionService.searchTransactions(
                criteria, 0, 10, "createdAt", "desc"
        );

        assertNotNull(result);
        assertEquals(0, result.getTransactions().size());
        assertEquals(0, result.getTotalRecords());
        assertEquals(BigDecimal.ZERO, result.getTotalBalance());
    }

    @Test
    void shouldSearchTransactionsWhenOnlyFromDateIsProvided() {
        Page<Transaction> page = new PageImpl<>(Arrays.asList(transaction, createTransaction(new BigDecimal("200.00"))));

        when(customTransactionRepository.findTransactionsByCriteria(
                eq("Aylin"), eq(null), eq(null),
                eq(LocalDate.of(2025, 3, 1)), eq(null), eq(null), eq(null), any(PageRequest.class))
        ).thenReturn(page);

        when(customTransactionRepository.calculateTotalBalanceByCriteria(
                eq("Aylin"), eq(null), eq(null),
                eq(LocalDate.of(2025, 3, 1)), eq(null), eq(null), eq(null))
        ).thenReturn(new BigDecimal("300.00"));

        TransactionSearchCriteriaDTO criteria = new TransactionSearchCriteriaDTO();
        criteria.setAccountName("Aylin");
        criteria.setFromDate(LocalDate.of(2025, 3, 1));

        var result = transactionService.searchTransactions(
                criteria, 0, 10, "createdAt", "asc"
        );

        assertNotNull(result);
        assertEquals(2, result.getTransactions().size());
        assertEquals(300.00, result.getTotalBalance().doubleValue(), 0.001);
    }

    @Test
    void shouldSearchTransactionsWhenOnlyToDateIsProvided() {
        Page<Transaction> page = new PageImpl<>(Collections.singletonList(transaction));

        when(customTransactionRepository.findTransactionsByCriteria(
                eq("Aylin"), eq(null), eq(null),
                eq(null), eq(LocalDate.of(2025, 3, 2)), eq(null), eq(null), any(PageRequest.class))
        ).thenReturn(page);

        when(customTransactionRepository.calculateTotalBalanceByCriteria(
                eq("Aylin"), eq(null), eq(null),
                eq(null), eq(LocalDate.of(2025, 3, 2)), eq(null), eq(null))
        ).thenReturn(new BigDecimal("100.00"));

        TransactionSearchCriteriaDTO criteria = new TransactionSearchCriteriaDTO();
        criteria.setAccountName("Aylin");
        criteria.setToDate(LocalDate.of(2025, 3, 2));

        var result = transactionService.searchTransactions(
                criteria, 0, 10, "createdAt", "asc"
        );

        assertNotNull(result);
        assertEquals(1, result.getTransactions().size());
        assertEquals(100.00, result.getTotalBalance().doubleValue(), 0.001);
    }

    private Transaction createTransaction(BigDecimal amount) {
        Transaction t = new Transaction();
        t.setAmount(amount);
        t.setAccountName("Aylin");
        t.setCreatedAt(LocalDateTime.now());
        return t;
    }
}
