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

    public BigDecimal calculateBalance(String accountName) {
        return transactionRepository.findByAccountName(accountName).stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

