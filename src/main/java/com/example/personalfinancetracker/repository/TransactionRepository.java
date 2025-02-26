package com.example.personalfinancetracker.repository;

import com.example.personalfinancetracker.domain.Transaction;
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

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.accountName = :accountName AND CAST(t.createdAt AS date) <= :date")
    BigDecimal calculateBalanceForAccount(
            @Param("accountName") String accountName,
            @Param("date") LocalDate date
    );
}

