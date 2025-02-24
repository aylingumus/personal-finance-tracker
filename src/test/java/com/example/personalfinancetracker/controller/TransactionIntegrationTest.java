package com.example.personalfinancetracker.controller;

import com.example.personalfinancetracker.domain.Transaction;
import com.example.personalfinancetracker.dto.TransactionRequestDTO;
import com.example.personalfinancetracker.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionIntegrationTest {

    private static final String API_PREFIX = "/api/v1/transactions";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        Objects.requireNonNull(cacheManager.getCache("balanceCache")).clear();
    }

    @Test
    public void shouldAddTransactionSuccessfully() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setAccountName("Aylin");
        request.setAmount(BigDecimal.valueOf(50));
        request.setCategory("Income");
        request.setDescription("Salary");

        mockMvc.perform(post(API_PREFIX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.accountName", is("Aylin")))
                .andExpect(jsonPath("$.amount", is(50)));
    }

    @Test
    public void shouldReturnTransactionsForSpecifiedAccount() throws Exception {
        var tx1 = createAndSaveTransaction("Aylin", BigDecimal.valueOf(50), "Income", "Salary");
        var tx2 = createAndSaveTransaction("Aylin", BigDecimal.valueOf(-10), "Expense", "Groceries");
        var tx3 = createAndSaveTransaction("Nazli", BigDecimal.valueOf(30), "Income", "Bonus");

        mockMvc.perform(get(API_PREFIX + "/account/Aylin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].accountName", is("Aylin")));
    }

    @Test
    public void shouldUpdateTransactionSuccessfully() throws Exception {
        var tx = createAndSaveTransaction("Aylin", BigDecimal.valueOf(50), "Income", "Salary");

        TransactionRequestDTO updateRequest = new TransactionRequestDTO();
        updateRequest.setAccountName("Aylin");
        updateRequest.setAmount(BigDecimal.valueOf(75));
        updateRequest.setCategory("Income");
        updateRequest.setDescription("Updated Salary");

        mockMvc.perform(put(API_PREFIX + "/" + tx.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(75))
                .andExpect(jsonPath("$.description").value("Updated Salary"));
    }

    @Test
    public void shouldReturnCorrectBalanceForAccountOnGivenDate() throws Exception {
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(50),
                "Income", "Salary", LocalDateTime.of(2025, 2, 10, 10, 0));
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(-20),
                "Expense", "Groceries", LocalDateTime.of(2025, 2, 15, 12, 0));

        mockMvc.perform(get(API_PREFIX + "/balance/Aylin")
                        .param("date", LocalDate.of(2025, 2, 15).toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("30.00"));
    }

    @Test
    public void shouldReturnCorrectBalanceForAccountWithoutGivenDate() throws Exception {
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(50),
                "Income", "Salary", LocalDateTime.of(2025, 2, 10, 10, 0));
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(-20),
                "Expense", "Groceries", LocalDateTime.of(2025, 2, 15, 12, 0));

        mockMvc.perform(get(API_PREFIX + "/balance/Aylin"))
                .andExpect(status().isOk())
                .andExpect(content().string("30.00"));
    }

    @Test
    public void shouldSearchTransactionsWithMultipleCriteria() throws Exception {
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(50),
                "Grocery", "Supermarket", LocalDateTime.of(2025, 2, 10, 10, 0));
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(-20),
                "Grocery", "Market shopping", LocalDateTime.of(2025, 2, 15, 12, 0));
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(-100),
                "Utilities", "Electricity bill", LocalDateTime.of(2025, 2, 20, 14, 0));

        mockMvc.perform(get(API_PREFIX)
                        .param("accountName", "Aylin")
                        .param("fromDate", "2025-02-01")
                        .param("toDate", "2025-02-28")
                        .param("category", "Grocery")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(2)))
                .andExpect(jsonPath("$.totalRecords", is(2)))
                .andExpect(jsonPath("$.totalBalance", is(30.0)));
    }

    @Test
    public void shouldSearchTransactionsWithNoCriteriaReturnsAll() throws Exception {
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(50),
                "Income", "Salary", LocalDateTime.of(2025, 1, 10, 10, 0));
        createAndSaveTransaction("Nazli", BigDecimal.valueOf(20),
                "Income", "Gift", LocalDateTime.of(2025, 1, 11, 12, 0));

        mockMvc.perform(get(API_PREFIX))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(2)))
                .andExpect(jsonPath("$.totalRecords", is(2)));
    }

    @Test
    public void shouldSearchTransactionsWithOnlyFromDate() throws Exception {
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(50), "Income", "Salary",
                LocalDateTime.of(2025, 3, 1, 9, 0));
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(75), "Food", "Dinner",
                LocalDateTime.of(2025, 3, 2, 11, 0));

        mockMvc.perform(get(API_PREFIX)
                        .param("accountName", "Aylin")
                        .param("fromDate", "2025-03-01")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(2)))
                .andExpect(jsonPath("$.totalRecords", is(2)));
    }

    @Test
    public void shouldSearchTransactionsWithOnlyToDate() throws Exception {
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(50), "Income", "Salary",
                LocalDateTime.of(2025, 3, 1, 9, 0));
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(75), "Food", "Dinner",
                LocalDateTime.of(2025, 3, 2, 11, 0));

        mockMvc.perform(get(API_PREFIX)
                        .param("accountName", "Aylin")
                        .param("toDate", "2025-03-02")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(2)))
                .andExpect(jsonPath("$.totalRecords", is(2)));
    }

    @Test
    public void shouldReturnTransactionsWithNoMatchingCriteriaReturnsEmpty() throws Exception {
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(50), "Income", "Salary", LocalDateTime.of(2025, 2, 10, 10, 0));

        mockMvc.perform(get(API_PREFIX)
                        .param("accountName", "NonExistent")
                        .param("minAmount", "1000")
                        .param("fromDate", "2030-01-01")
                        .param("toDate", "2030-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(0)))
                .andExpect(jsonPath("$.totalRecords", is(0)));
    }

    @Test
    public void shouldReturnTransactionsWithMinAmountFilter() throws Exception {
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(80), "Food", "Snack", LocalDateTime.of(2025, 1, 10, 10, 0));
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(120), "Food", "Lunch", LocalDateTime.of(2025, 1, 15, 12, 0));
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(150), "Food", "Dinner", LocalDateTime.now());

        mockMvc.perform(get(API_PREFIX)
                        .param("accountName", "Aylin")
                        .param("minAmount", "100")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(2)))
                .andExpect(jsonPath("$.totalRecords", is(2)))
                .andExpect(jsonPath("$.transactions[0].accountName", is("Aylin")))
                .andExpect(jsonPath("$.transactions[0].amount", is(120.0)));
    }

    @Test
    public void shouldReturnTransactionsWithMaxAmountFilter() throws Exception {
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(100), "Food", "Lunch", LocalDateTime.of(2025, 3, 1, 10, 0));
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(200), "Food", "Dinner", LocalDateTime.of(2025, 3, 2, 10, 0));

        mockMvc.perform(get(API_PREFIX)
                        .param("accountName", "Aylin")
                        .param("maxAmount", "150")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(1)))
                .andExpect(jsonPath("$.totalRecords", is(1)))
                .andExpect(jsonPath("$.transactions[0].accountName", is("Aylin")))
                .andExpect(jsonPath("$.transactions[0].amount", is(100.0)));
    }

    @Test
    public void shouldReturnTransactionsWithOnlyDescriptionParam() throws Exception {
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(75), "Income", "Freelance job", LocalDateTime.of(2025, 2, 10, 10, 0));
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(25), "Expense", "Restaurant lunch", LocalDateTime.of(2025, 2, 12, 10, 0));

        mockMvc.perform(get(API_PREFIX)
                        .param("description", "freeLanCe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(1)))
                .andExpect(jsonPath("$.totalRecords", is(1)))
                .andExpect(jsonPath("$.transactions[0].amount", is(75.0)));
    }

    @Test
    public void shouldCacheBalanceCalculation() throws Exception {
        createAndSaveTransaction("Aylin", BigDecimal.valueOf(100), "Income", "Salary");

        // First call should calculate and cache the balance
        mockMvc.perform(get(API_PREFIX + "/balance/Aylin")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("100.00"));

        // Second call should return the cached value
        mockMvc.perform(get(API_PREFIX + "/balance/Aylin")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("100.00"));

        BigDecimal cachedBalance = Objects.requireNonNull(cacheManager.getCache("balanceCache"))
                .get("Aylin_" + LocalDate.now().toString(), BigDecimal.class);
        assertNotNull(cachedBalance);
        assertEquals(new BigDecimal("100.00"), cachedBalance);
    }

    private Transaction createAndSaveTransaction(String accountName, BigDecimal amount, String category,
                                                 String description) {
        return createAndSaveTransaction(accountName, amount, category, description, LocalDateTime.now());
    }

    private Transaction createAndSaveTransaction(String accountName, BigDecimal amount, String category,
                                                 String description, LocalDateTime createdAt) {
        Transaction tx = new Transaction();
        tx.setAccountName(accountName);
        tx.setAmount(amount);
        tx.setCreatedAt(createdAt);
        tx.setCategory(category);
        tx.setDescription(description);
        return transactionRepository.saveAndFlush(tx);
    }
}