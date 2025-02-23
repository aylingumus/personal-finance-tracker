package com.example.personalfinancetracker.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String accountName;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String category;
    private String description;
    @Version
    private Long version;
}

