package com.example.personalfinancetracker.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiConstant {
    API_PREFIX("/api/v1/transactions");

    private final String value;
}

