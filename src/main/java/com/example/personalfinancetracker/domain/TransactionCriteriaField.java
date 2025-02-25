package com.example.personalfinancetracker.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionCriteriaField {
    ACCOUNT_NAME("accountName"),
    AMOUNT("amount"),
    CREATED_AT("createdAt"),
    CATEGORY("category"),
    DESCRIPTION("description");

    private final String fieldName;
}
