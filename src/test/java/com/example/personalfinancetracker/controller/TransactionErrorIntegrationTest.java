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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionErrorIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(TransactionErrorIntegrationTest.class);

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

        mockMvc.perform(post(API_PREFIX)
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

        mockMvc.perform(put(API_PREFIX + "/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Transaction not found for id: 9999")));
    }

    @Test
    public void shouldReturnNotFoundWhenDeletingNonExistentTransaction() throws Exception {
        mockMvc.perform(delete(API_PREFIX + "/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Transaction not found for id: 9999")));
    }

    @Test
    public void shouldReturnErrorWhenGettingBalanceWithInvalidDateFormat() throws Exception {
        mockMvc.perform(get(API_PREFIX + "/balance/Aylin")
                        .param("date", "23/02/2025")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid parameter: date")));
    }

    @Test
    public void shouldReturnErrorForNegativePageNumber() throws Exception {
        mockMvc.perform(get(API_PREFIX)
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Page index must not be less than zero")));
    }

    @Test
    public void shouldReturnErrorForInvalidDateRangeInSearch() throws Exception {
        mockMvc.perform(get(API_PREFIX)
                        .param("fromDate", "2025-03-15")
                        .param("toDate", "2025-02-15"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("From date cannot be after to date")));
    }

    @Test
    public void shouldReturnErrorWhenRequestingBalanceForNonExistentAccount() throws Exception {
        mockMvc.perform(get(API_PREFIX + "/balance/Alien"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("No transactions found for account: Alien")));
    }

    @Test
    public void shouldReturnErrorWhenUpdatingNonExistentTransactionById() throws Exception {
        TransactionRequestDTO updateRequest = new TransactionRequestDTO();
        updateRequest.setAccountName("Aylin");
        updateRequest.setAmount(BigDecimal.valueOf(150.00));
        updateRequest.setCategory("Income");
        updateRequest.setDescription("Updated Salary");

        mockMvc.perform(put(API_PREFIX + "/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Transaction not found for id: 9999")));
    }

    @Test
    public void shouldHandleConcurrentUpdates() throws Exception {
        Transaction tx = createAndSaveTransaction(transactionRepository,"Aylin", BigDecimal.valueOf(100), "Income", "Salary", LocalDateTime.now());

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            List<Integer> statuses = Collections.synchronizedList(new ArrayList<>());

            Runnable updateTask = () -> {
                TransactionRequestDTO updateRequest = new TransactionRequestDTO();
                updateRequest.setAccountName("Aylin");
                updateRequest.setAmount(BigDecimal.valueOf(150));
                updateRequest.setCategory("Income");
                updateRequest.setDescription("Updated Salary");
                try {
                    int status = mockMvc.perform(put(API_PREFIX.getValue() + "/" + tx.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                            .andReturn().getResponse().getStatus();
                    statuses.add(status);
                } catch (Exception e) {
                    log.error("Error during concurrent update task", e);
                }
            };

            executor.execute(updateTask);
            executor.execute(updateTask);
            executor.shutdown();
            boolean terminated = executor.awaitTermination(1, TimeUnit.MINUTES);
            assertTrue(terminated, "Executor did not terminate in the expected time");

            // Expect one update to succeed (200) and one to fail (409)
            assertTrue(statuses.contains(200));
            assertTrue(statuses.contains(409));

            Transaction updatedTx = transactionRepository.findById(tx.getId()).orElseThrow();
            assertEquals(0, BigDecimal.valueOf(150.0).compareTo(updatedTx.getAmount()));
            // Verify version incremented to 1
            assertEquals(1, updatedTx.getVersion());
        }
    }
}
