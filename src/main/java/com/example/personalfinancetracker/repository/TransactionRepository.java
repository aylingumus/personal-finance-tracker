package com.example.personalfinancetracker.repository;

import com.example.personalfinancetracker.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountName(String accountName);

    @Query("SELECT t FROM Transaction t WHERE t.accountName = :accountName AND CAST(t.timestamp AS date) <= :date")
    List<Transaction> findByAccountNameAndDate(
            @Param("accountName") String accountName,
            @Param("date") LocalDate date
    );

    @Query("SELECT t FROM Transaction t " +
            "WHERE (:accountName IS NULL OR t.accountName = :accountName) " +
            "AND (:minAmount IS NULL OR t.amount >= :minAmount) " +
            "AND (:maxAmount IS NULL OR t.amount <= :maxAmount) " +
            "AND (:fromDate IS NULL OR CAST(t.timestamp AS date) >= :fromDate) " +
            "AND (:toDate IS NULL OR CAST(t.timestamp AS date) <= :toDate) " +
            "AND (:category IS NULL OR t.category = :category) " +
            "AND (:description IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%')))")
    Page<Transaction> findFilteredTransactions(
            @Param("accountName") String accountName,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("category") String category,
            @Param("description") String description,
            Pageable pageable);
}

