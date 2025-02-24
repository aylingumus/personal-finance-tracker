package com.example.personalfinancetracker.controller;

import com.example.personalfinancetracker.dto.TransactionRequestDTO;
import com.example.personalfinancetracker.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionErrorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    @Test
    public void shouldReturnValidationErrorWhenAddingTransactionWithMissingFields() throws Exception {
        String invalidRequest = "{\"description\": \"Salary\"}";

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Amount is required")))
                .andExpect(jsonPath("$.message", containsString("Account name is required")))
                .andExpect(jsonPath("$.message", containsString("Category is required")));
    }

    @Test
    public void shouldReturnErrorWhenUpdatingNonExistingTransaction() throws Exception {
        TransactionRequestDTO updateRequest = new TransactionRequestDTO();
        updateRequest.setAccountName("Aylin");
        updateRequest.setAmount(BigDecimal.valueOf(150));
        updateRequest.setCategory("Income");
        updateRequest.setDescription("Updated Salary");

        mockMvc.perform(put("/transactions/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Transaction not found for id: 9999")));
    }

    @Test
    public void shouldReturnErrorWhenGettingBalanceWithInvalidDateFormat() throws Exception {
        mockMvc.perform(get("/transactions/balance/Aylin")
                        .param("date", "23/02/2025")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid parameter: date")));
    }

    @Test
    public void shouldReturnErrorForNegativePageNumber() throws Exception {
        mockMvc.perform(get("/transactions")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Page index must not be less than zero")));
    }

    @Test
    public void shouldReturnErrorForInvalidDateRangeInSearch() throws Exception {
        mockMvc.perform(get("/transactions")
                        .param("fromDate", "2025-03-15")
                        .param("toDate", "2025-02-15"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("From date cannot be after to date")));
    }

    @Test
    public void shouldReturnErrorWhenRequestingBalanceForNonExistentAccount() throws Exception {
        mockMvc.perform(get("/transactions/balance/NonExistentAccount"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("No transactions found for account: NonExistentAccount")));
    }

    @Test
    public void shouldReturnErrorWhenUpdatingNonExistentTransactionById() throws Exception {
        TransactionRequestDTO updateRequest = new TransactionRequestDTO();
        updateRequest.setAccountName("Aylin");
        updateRequest.setAmount(BigDecimal.valueOf(150.00));
        updateRequest.setCategory("Income");
        updateRequest.setDescription("Updated Salary");

        mockMvc.perform(put("/transactions/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Transaction not found for id: 9999")));
    }
}
