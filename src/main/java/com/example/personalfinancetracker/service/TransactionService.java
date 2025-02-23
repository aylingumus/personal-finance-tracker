package com.example.personalfinancetracker.service;

import com.example.personalfinancetracker.domain.Transaction;
import com.example.personalfinancetracker.dto.TransactionRequestDTO;
import com.example.personalfinancetracker.dto.TransactionResponseDTO;
import com.example.personalfinancetracker.mapper.TransactionMapper;
import com.example.personalfinancetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;


    @Transactional
    public TransactionResponseDTO addTransaction(TransactionRequestDTO requestDTO) {
        Transaction transaction = transactionMapper.toEntity(requestDTO);
        Transaction savedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toDTO(savedTransaction);
    }

    public List<TransactionResponseDTO> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        return transactions.stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<TransactionResponseDTO> getTransactionsByAccount(String accountName) {
        List<Transaction> transactions = transactionRepository.findByAccountName(accountName);
        return transactions.stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public BigDecimal calculateBalance(String accountName, LocalDate date) {
        return transactionRepository
                .findByAccountNameAndDate(accountName, date)
                .stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public TransactionResponseDTO updateTransaction(Long id, TransactionRequestDTO requestDTO) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        transaction.setAccountName(requestDTO.getAccountName());
        transaction.setAmount(requestDTO.getAmount());
        transaction.setCategory(requestDTO.getCategory());
        transaction.setDescription(requestDTO.getDescription());
        // TO-DO: Maybe add a field as updatedAt, or update the timestamp or keep the original

        Transaction updated = transactionRepository.save(transaction);
        return transactionMapper.toDTO(updated);
    }
}

