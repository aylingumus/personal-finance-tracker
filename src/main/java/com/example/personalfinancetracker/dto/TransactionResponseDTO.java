package com.example.personalfinancetracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponseDTO {
    private Long id;
    private String accountName;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String category;
    private String description;
}
