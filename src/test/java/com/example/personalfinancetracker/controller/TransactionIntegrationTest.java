package com.example.personalfinancetracker.controller;

import com.example.personalfinancetracker.domain.Transaction;
import com.example.personalfinancetracker.dto.TransactionRequestDTO;
import com.example.personalfinancetracker.repository.CustomTransactionRepository;
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CustomTransactionRepository customTransactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    @Test
    public void shouldAddTransactionSuccessfully() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setAccountName("Aylin");
        request.setAmount(BigDecimal.valueOf(50));
        request.setCategory("Income");
        request.setDescription("Salary");

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.accountName", is("Aylin")))
                .andExpect(jsonPath("$.amount", is(50)));
    }

    @Test
    public void shouldReturnTransactionsForSpecifiedAccount() throws Exception {
        var tx1 = new Transaction();
        tx1.setAccountName("Aylin");
        tx1.setAmount(BigDecimal.valueOf(50));
        tx1.setCreatedAt(LocalDateTime.now());
        tx1.setCategory("Income");
        tx1.setDescription("Salary");
        transactionRepository.saveAndFlush(tx1);

        var tx2 = new Transaction();
        tx2.setAccountName("Aylin");
        tx2.setAmount(BigDecimal.valueOf(-10));
        tx2.setCreatedAt(LocalDateTime.now());
        tx2.setCategory("Expense");
        tx2.setDescription("Groceries");
        transactionRepository.saveAndFlush(tx2);

        var tx3 = new Transaction();
        tx3.setAccountName("Nazli");
        tx3.setAmount(BigDecimal.valueOf(30));
        tx3.setCreatedAt(LocalDateTime.now());
        tx3.setCategory("Income");
        tx3.setDescription("Bonus");
        transactionRepository.saveAndFlush(tx3);

        mockMvc.perform(get("/transactions/account/Aylin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].accountName", is("Aylin")));
    }

    @Test
    public void shouldReturnCorrectBalanceForAccountOnGivenDate() throws Exception {
        var tx1 = new Transaction();
        tx1.setAccountName("Aylin");
        tx1.setAmount(BigDecimal.valueOf(50));
        tx1.setCreatedAt(LocalDateTime.of(2025, 2, 10, 10, 0));
        tx1.setCategory("Income");
        tx1.setDescription("Salary");
        transactionRepository.saveAndFlush(tx1);

        var tx2 = new Transaction();
        tx2.setAccountName("Aylin");
        tx2.setAmount(BigDecimal.valueOf(-20));
        tx2.setCreatedAt(LocalDateTime.of(2025, 2, 15, 12, 0));
        tx2.setCategory("Expense");
        tx2.setDescription("Groceries");
        transactionRepository.saveAndFlush(tx2);

        mockMvc.perform(get("/transactions/balance/Aylin")
                        .param("date", LocalDate.of(2025, 2, 15).toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("30.00"));
    }

    @Test
    public void shouldReturnCorrectBalanceForAccountWithoutGivenDate() throws Exception {
        Objects.requireNonNull(cacheManager.getCache("balanceCache")).clear();

        var tx1 = new Transaction();
        tx1.setAccountName("Aylin");
        tx1.setAmount(BigDecimal.valueOf(50));
        tx1.setCreatedAt(LocalDateTime.of(2025, 2, 10, 10, 0));
        tx1.setCategory("Income");
        tx1.setDescription("Salary");
        transactionRepository.saveAndFlush(tx1);

        var tx2 = new Transaction();
        tx2.setAccountName("Aylin");
        tx2.setAmount(BigDecimal.valueOf(-20));
        tx2.setCreatedAt(LocalDateTime.of(2025, 2, 15, 12, 0));
        tx2.setCategory("Expense");
        tx2.setDescription("Groceries");
        transactionRepository.saveAndFlush(tx2);

        mockMvc.perform(get("/transactions/balance/Aylin"))
                .andExpect(status().isOk())
                .andExpect(content().string("30.00"));
    }

    @Test
    public void shouldUpdateTransactionSuccessfully() throws Exception {
        var tx = new Transaction();
        tx.setAccountName("Aylin");
        tx.setAmount(BigDecimal.valueOf(50));
        tx.setCreatedAt(LocalDateTime.now());
        tx.setCategory("Income");
        tx.setDescription("Salary");
        var savedTx = transactionRepository.saveAndFlush(tx);

        TransactionRequestDTO updateRequest = new TransactionRequestDTO();
        updateRequest.setAccountName("Aylin");
        updateRequest.setAmount(BigDecimal.valueOf(75));
        updateRequest.setCategory("Income");
        updateRequest.setDescription("Updated Salary");

        mockMvc.perform(put("/transactions/" + savedTx.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(75))
                .andExpect(jsonPath("$.description").value("Updated Salary"));
    }

    @Test
    public void shouldSearchTransactionsByCategoryAndDateSortByAsc() throws Exception {
        var tx1 = new Transaction();
        tx1.setAccountName("Aylin");
        tx1.setAmount(BigDecimal.valueOf(50));
        tx1.setCreatedAt(LocalDateTime.of(2025, 2, 10, 10, 0));
        tx1.setCategory("Grocery");
        tx1.setDescription("Supermarket");
        transactionRepository.saveAndFlush(tx1);

        var tx2 = new Transaction();
        tx2.setAccountName("Aylin");
        tx2.setAmount(BigDecimal.valueOf(-20));
        tx2.setCreatedAt(LocalDateTime.of(2025, 2, 15, 12, 0));
        tx2.setCategory("Grocery");
        tx2.setDescription("Market shopping");
        transactionRepository.saveAndFlush(tx2);

        var tx3 = new Transaction();
        tx3.setAccountName("Aylin");
        tx3.setAmount(BigDecimal.valueOf(-100));
        tx3.setCreatedAt(LocalDateTime.of(2025, 2, 20, 14, 0));
        tx3.setCategory("Utilities");
        tx3.setDescription("Electricity bill");
        transactionRepository.saveAndFlush(tx3);

        mockMvc.perform(get("/transactions")
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
    public void shouldSearchTransactionsByCategoryAndDateSortByDesc() throws Exception {
        var tx1 = new Transaction();
        tx1.setAccountName("Aylin");
        tx1.setAmount(BigDecimal.valueOf(50));
        tx1.setCreatedAt(LocalDateTime.of(2025, 2, 10, 10, 0));
        tx1.setCategory("Grocery");
        tx1.setDescription("Supermarket");
        transactionRepository.saveAndFlush(tx1);

        var tx2 = new Transaction();
        tx2.setAccountName("Aylin");
        tx2.setAmount(BigDecimal.valueOf(-20));
        tx2.setCreatedAt(LocalDateTime.of(2025, 2, 15, 12, 0));
        tx2.setCategory("Grocery");
        tx2.setDescription("Market shopping");
        transactionRepository.saveAndFlush(tx2);

        var tx3 = new Transaction();
        tx3.setAccountName("Aylin");
        tx3.setAmount(BigDecimal.valueOf(-100));
        tx3.setCreatedAt(LocalDateTime.of(2025, 2, 20, 14, 0));
        tx3.setCategory("Utilities");
        tx3.setDescription("Electricity bill");
        transactionRepository.saveAndFlush(tx3);

        mockMvc.perform(get("/transactions")
                        .param("accountName", "Aylin")
                        .param("fromDate", "2025-02-01")
                        .param("toDate", "2025-02-28")
                        .param("category", "Grocery")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(2)))
                .andExpect(jsonPath("$.totalRecords", is(2)))
                .andExpect(jsonPath("$.totalBalance", is(30.0)));
    }

    @Test
    public void shouldReturnTransactionsSortedByCreatedAtAsc() throws Exception {
        Transaction tx1 = new Transaction();
        tx1.setAccountName("Aylin");
        tx1.setAmount(BigDecimal.valueOf(50));
        tx1.setCreatedAt(LocalDateTime.of(2025, 3, 1, 9, 0));
        tx1.setCategory("Food");
        tx1.setDescription("Breakfast");
        transactionRepository.saveAndFlush(tx1);

        Transaction tx2 = new Transaction();
        tx2.setAccountName("Aylin");
        tx2.setAmount(BigDecimal.valueOf(75));
        tx2.setCreatedAt(LocalDateTime.of(2025, 3, 1, 11, 0));
        tx2.setCategory("Food");
        tx2.setDescription("Dinner");
        transactionRepository.saveAndFlush(tx2);

        mockMvc.perform(get("/transactions")
                        .param("accountName", "Aylin")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(2)))
                .andExpect(jsonPath("$.transactions[0].createdAt", containsString("2025-03-01T09")))
                .andExpect(jsonPath("$.transactions[1].createdAt", containsString("2025-03-01T11")));
    }


    @Test
    public void shouldSearchTransactionsWithNoCriteriaReturnsAll() throws Exception {
        var tx1 = new Transaction();
        tx1.setAccountName("Aylin");
        tx1.setAmount(BigDecimal.valueOf(50));
        tx1.setCreatedAt(LocalDateTime.of(2025, 1, 10, 10, 0));
        tx1.setCategory("Income");
        tx1.setDescription("Salary");
        transactionRepository.saveAndFlush(tx1);

        var tx2 = new Transaction();
        tx2.setAccountName("Nazli");
        tx2.setAmount(BigDecimal.valueOf(20));
        tx2.setCreatedAt(LocalDateTime.of(2025, 1, 11, 12, 0));
        tx2.setCategory("Income");
        tx2.setDescription("Gift");
        transactionRepository.saveAndFlush(tx2);

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(2)))
                .andExpect(jsonPath("$.totalRecords", is(2)));
    }

    @Test
    public void shouldSearchTransactionsWithNoMatchingCriteriaReturnsEmpty() throws Exception {
        var tx1 = new Transaction();
        tx1.setAccountName("Aylin");
        tx1.setAmount(BigDecimal.valueOf(50));
        tx1.setCreatedAt(LocalDateTime.of(2025, 2, 10, 10, 0));
        tx1.setCategory("Income");
        tx1.setDescription("Salary");
        transactionRepository.saveAndFlush(tx1);

        mockMvc.perform(get("/transactions")
                        .param("accountName", "NonExistent")
                        .param("minAmount", "1000")
                        .param("fromDate", "2030-01-01")
                        .param("toDate", "2030-01-31")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(0)))
                .andExpect(jsonPath("$.totalRecords", is(0)));
    }

    @Test
    public void shouldSearchTransactionsWithMinAmountFilter() throws Exception {
        var tx1 = new Transaction();
        tx1.setAccountName("Aylin");
        tx1.setAmount(BigDecimal.valueOf(50));
        tx1.setCreatedAt(LocalDateTime.of(2025, 1, 10, 10, 0));
        tx1.setCategory("Expense");
        tx1.setDescription("Groceries");
        transactionRepository.saveAndFlush(tx1);

        var tx2 = new Transaction();
        tx2.setAccountName("Aylin");
        tx2.setAmount(BigDecimal.valueOf(150));
        tx2.setCreatedAt(LocalDateTime.of(2025, 1, 15, 12, 0));
        tx2.setCategory("Income");
        tx2.setDescription("Salary");
        transactionRepository.saveAndFlush(tx2);

        mockMvc.perform(get("/transactions")
                        .param("accountName", "Aylin")
                        .param("minAmount", "100")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(1)))
                .andExpect(jsonPath("$.totalRecords", is(1)))
                .andExpect(jsonPath("$.totalBalance", is(150.0)));
    }

    @Test
    public void shouldSearchTransactionsWithMaxAmountFilter() throws Exception {
        var tx1 = new Transaction();
        tx1.setAccountName("Aylin");
        tx1.setAmount(BigDecimal.valueOf(100));
        tx1.setCreatedAt(LocalDateTime.of(2025, 3, 1, 10, 0));
        tx1.setCategory("Food");
        tx1.setDescription("Lunch");
        transactionRepository.saveAndFlush(tx1);

        var tx2 = new Transaction();
        tx2.setAccountName("Aylin");
        tx2.setAmount(BigDecimal.valueOf(200));
        tx2.setCreatedAt(LocalDateTime.of(2025, 3, 2, 10, 0));
        tx2.setCategory("Food");
        tx2.setDescription("Dinner");
        transactionRepository.saveAndFlush(tx2);

        mockMvc.perform(get("/transactions")
                        .param("accountName", "Aylin")
                        .param("maxAmount", "150")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(1)))
                .andExpect(jsonPath("$.totalRecords", is(1)))
                .andExpect(jsonPath("$.totalBalance", is(100.0)));
    }

    @Test
    public void shouldSearchTransactionsWithFromDateAfterToDateReturnsEmpty() throws Exception {
        var tx1 = new Transaction();
        tx1.setAccountName("Aylin");
        tx1.setAmount(BigDecimal.valueOf(50));
        tx1.setCreatedAt(LocalDateTime.of(2025, 2, 10, 10, 0));
        tx1.setCategory("Income");
        tx1.setDescription("Salary");
        transactionRepository.saveAndFlush(tx1);

        mockMvc.perform(get("/transactions")
                        .param("accountName", "Aylin")
                        .param("fromDate", "2025-03-01")
                        .param("toDate", "2025-02-01")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(0)))
                .andExpect(jsonPath("$.totalRecords", is(0)))
                .andExpect(jsonPath("$.totalBalance", is(0)));
    }

    @Test
    public void shouldSearchTransactionsWithOnlyDescriptionParam() throws Exception {
        var tx1 = new Transaction();
        tx1.setAccountName("Aylin");
        tx1.setAmount(BigDecimal.valueOf(75));
        tx1.setCreatedAt(LocalDateTime.of(2025, 2, 10, 10, 0));
        tx1.setCategory("Income");
        tx1.setDescription("Freelance job");
        transactionRepository.saveAndFlush(tx1);

        var tx2 = new Transaction();
        tx2.setAccountName("Aylin");
        tx2.setAmount(BigDecimal.valueOf(25));
        tx2.setCreatedAt(LocalDateTime.of(2025, 2, 12, 10, 0));
        tx2.setCategory("Expense");
        tx2.setDescription("Restaurant lunch");
        transactionRepository.saveAndFlush(tx2);

        mockMvc.perform(get("/transactions")
                        .param("description", "freeLanCe")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(1)))
                .andExpect(jsonPath("$.totalRecords", is(1)))
                .andExpect(jsonPath("$.totalBalance", is(75.0)));
    }

    @Test
    public void shouldCacheBalanceCalculation() throws Exception {
        Objects.requireNonNull(cacheManager.getCache("balanceCache")).clear();

        Transaction tx = new Transaction();
        tx.setAccountName("Aylin");
        tx.setAmount(BigDecimal.valueOf(100));
        tx.setCreatedAt(LocalDateTime.now());
        tx.setCategory("Income");
        tx.setDescription("Salary");
        transactionRepository.saveAndFlush(tx);

        // First call should calculate and cache the balance
        mockMvc.perform(get("/transactions/balance/Aylin")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("100.00"));

        // Second call should return the cached value
        mockMvc.perform(get("/transactions/balance/Aylin")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("100.00"));

        BigDecimal cachedBalance = Objects.requireNonNull(cacheManager.getCache("balanceCache"))
                .get("Aylin_" + LocalDate.now().toString(), BigDecimal.class);
        assertNotNull(cachedBalance);
        assertEquals(new BigDecimal("100.00"), cachedBalance);
    }
}
