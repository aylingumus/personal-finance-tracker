package com.example.personalfinancetracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionRequestDTO {
    @NotBlank(message = "Account name is required")
    private String accountName;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotBlank(message = "Category is required")
    private String category;

    private String description;
}
