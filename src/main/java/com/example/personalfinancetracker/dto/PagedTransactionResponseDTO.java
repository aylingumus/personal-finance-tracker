package com.example.personalfinancetracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PagedTransactionResponseDTO {
    private List<TransactionResponseDTO> transactions;
    private long totalRecords;
    private BigDecimal totalSum;
}
