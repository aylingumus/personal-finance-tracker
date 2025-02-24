package com.example.personalfinancetracker.service;

import com.example.personalfinancetracker.domain.Transaction;
import com.example.personalfinancetracker.dto.PagedTransactionResponseDTO;
import com.example.personalfinancetracker.dto.TransactionRequestDTO;
import com.example.personalfinancetracker.dto.TransactionResponseDTO;
import com.example.personalfinancetracker.dto.TransactionSearchCriteriaDTO;
import com.example.personalfinancetracker.exception.TransactionNotFoundException;
import com.example.personalfinancetracker.mapper.TransactionMapper;
import com.example.personalfinancetracker.repository.CustomTransactionRepository;
import com.example.personalfinancetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final CustomTransactionRepository customTransactionRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    @CacheEvict(value = "balanceCache", allEntries = true)
    public TransactionResponseDTO addTransaction(TransactionRequestDTO requestDTO) {
        log.info("Adding new transaction for account: {}", requestDTO.getAccountName());
        Transaction transaction = transactionMapper.toEntity(requestDTO);
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction added successfully with ID: {}", savedTransaction.getId());
        return transactionMapper.toDTO(savedTransaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByAccount(String accountName) {
        log.info("Retrieving transactions for account: {}", accountName);
        List<Transaction> transactions = transactionRepository.findByAccountName(accountName);
        return transactions.stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "balanceCache", key = "#accountName + '_' + #date")
    public BigDecimal calculateBalance(String accountName, LocalDate date) {
        log.info("Calculating balance for account: {} as of date: {}", accountName, date);
        if (transactionRepository.findByAccountName(accountName).isEmpty()) {
            log.warn("Account not found when calculating balance: {}", accountName);
            throw new TransactionNotFoundException(accountName);
        }
        return transactionRepository.calculateBalanceForAccount(accountName, date);
    }

    @Transactional
    @CacheEvict(value = "balanceCache", allEntries = true)
    public TransactionResponseDTO updateTransaction(Long id, TransactionRequestDTO requestDTO) {
        log.info("Updating transaction with ID: {}", id);

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Transaction not found with ID: {}", id);
                    return new TransactionNotFoundException(id);
                });

        transaction.setAccountName(requestDTO.getAccountName());
        transaction.setAmount(requestDTO.getAmount());
        transaction.setCategory(requestDTO.getCategory());
        transaction.setDescription(requestDTO.getDescription());
        transaction.setUpdatedAt(LocalDateTime.now());

        Transaction updated = transactionRepository.save(transaction);
        log.info("Transaction updated successfully with ID: {}", updated.getId());
        return transactionMapper.toDTO(updated);
    }

    public PagedTransactionResponseDTO searchTransactions(
            TransactionSearchCriteriaDTO criteria,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        log.info("Searching transactions with criteria - account: {}, page: {}, size: {}",
                criteria.getAccountName(), page, size);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<Transaction> pageResult = customTransactionRepository.findTransactionsByCriteria(
                criteria.getAccountName(),
                criteria.getMinAmount(),
                criteria.getMaxAmount(),
                criteria.getFromDate(),
                criteria.getToDate(),
                criteria.getCategory(),
                criteria.getDescription(),
                pageable
        );

        List<TransactionResponseDTO> transactions = pageResult.getContent().stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());

        BigDecimal totalBalance = customTransactionRepository.calculateTotalBalanceByCriteria(
                criteria.getAccountName(),
                criteria.getMinAmount(),
                criteria.getMaxAmount(),
                criteria.getFromDate(),
                criteria.getToDate(),
                criteria.getCategory(),
                criteria.getDescription()
        );

        PagedTransactionResponseDTO response = new PagedTransactionResponseDTO();
        response.setTransactions(transactions);
        response.setTotalRecords(pageResult.getTotalElements());
        response.setTotalBalance(totalBalance);

        log.info("Search completed, found {} transactions", transactions.size());

        return response;
    }
}