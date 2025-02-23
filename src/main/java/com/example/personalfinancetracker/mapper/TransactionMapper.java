package com.example.personalfinancetracker.mapper;

import com.example.personalfinancetracker.domain.Transaction;
import com.example.personalfinancetracker.dto.TransactionRequestDTO;
import com.example.personalfinancetracker.dto.TransactionResponseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TransactionMapper {

    public Transaction toEntity(TransactionRequestDTO dto) {
        Transaction transaction = new Transaction();
        transaction.setAccountName(dto.getAccountName());
        transaction.setAmount(dto.getAmount());
        transaction.setCategory(dto.getCategory());
        transaction.setDescription(dto.getDescription());
        transaction.setTimestamp(LocalDateTime.now());
        return transaction;
    }

    public TransactionResponseDTO toDTO(Transaction entity) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setId(entity.getId());
        dto.setAccountName(entity.getAccountName());
        dto.setAmount(entity.getAmount());
        dto.setTimestamp(entity.getTimestamp());
        dto.setCategory(entity.getCategory());
        dto.setDescription(entity.getDescription());
        return dto;
    }
}
