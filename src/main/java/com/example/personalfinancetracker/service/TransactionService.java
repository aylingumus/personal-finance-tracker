package com.example.personalfinancetracker.service;

import com.example.personalfinancetracker.domain.Transaction;
import com.example.personalfinancetracker.dto.PagedTransactionResponseDTO;
import com.example.personalfinancetracker.dto.TransactionRequestDTO;
import com.example.personalfinancetracker.dto.TransactionResponseDTO;
import com.example.personalfinancetracker.dto.TransactionSearchCriteriaDTO;
import com.example.personalfinancetracker.mapper.TransactionMapper;
import com.example.personalfinancetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
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

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;


    @Transactional
    public TransactionResponseDTO addTransaction(TransactionRequestDTO requestDTO) {
        Transaction transaction = transactionMapper.toEntity(requestDTO);
        Transaction savedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toDTO(savedTransaction);
        // TO-DO: cache'e yeni hesaplanan güncel balance'ı koyabilirim
        // TO-DO: get balance endpointi de cache'ten çeker
        // TO-DO: update'e de cache güncelleme işlemini yapmam lazım
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByAccount(String accountName) {
        List<Transaction> transactions = transactionRepository.findByAccountName(accountName);
        return transactions.stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateBalance(String accountName, LocalDate date) {
        // TO-DO: Make the calculations in db level not here
        // TO-DO: (Optionally) Add cache - calculate today's balance
        return transactionRepository
                .findByAccountNameAndCreatedAt(accountName, date)
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
        transaction.setUpdatedAt(LocalDateTime.now());

        Transaction updated = transactionRepository.save(transaction);
        return transactionMapper.toDTO(updated);

//        try {
//            Transaction updated = transactionRepository.save(transaction);
//            return transactionMapper.toDTO(updated);
//        } catch (OptimisticLockException e) {
//            // TO-DO: Add log
//            throw e;
//        }
    }

    public PagedTransactionResponseDTO searchTransactions(
            TransactionSearchCriteriaDTO criteria,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<Transaction> pageResult = transactionRepository.findFilteredTransactions(
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

        BigDecimal totalBalance = pageResult.getContent().stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PagedTransactionResponseDTO response = new PagedTransactionResponseDTO();
        response.setTransactions(transactions);
        response.setTotalRecords(pageResult.getTotalElements());
        response.setTotalBalance(totalBalance);

        return response;
    }
}

