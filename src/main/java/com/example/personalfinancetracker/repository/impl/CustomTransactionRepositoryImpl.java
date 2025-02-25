package com.example.personalfinancetracker.repository.impl;

import com.example.personalfinancetracker.domain.Transaction;
import com.example.personalfinancetracker.repository.CustomTransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.example.personalfinancetracker.domain.TransactionCriteriaField.*;

@Repository
public class CustomTransactionRepositoryImpl implements CustomTransactionRepository {

    private final EntityManager entityManager;

    public CustomTransactionRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<Transaction> root,
                                        String accountName, BigDecimal minAmount, BigDecimal maxAmount,
                                        LocalDate fromDate, LocalDate toDate, String category, String description) {
        List<Predicate> predicates = new ArrayList<>();

        if (accountName != null) {
            predicates.add(cb.equal(root.get(ACCOUNT_NAME.getFieldName()), accountName));
        }
        if (minAmount != null) {
            predicates.add(cb.ge(root.get(AMOUNT.getFieldName()), minAmount));
        }
        if (maxAmount != null) {
            predicates.add(cb.le(root.get(AMOUNT.getFieldName()), maxAmount));
        }
        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(CREATED_AT.getFieldName()).as(LocalDate.class), fromDate));
        }
        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(CREATED_AT.getFieldName()).as(LocalDate.class), toDate));
        }
        if (category != null) {
            predicates.add(cb.equal(root.get(CATEGORY.getFieldName()), category));
        }
        if (description != null) {
            predicates.add(cb.like(cb.lower(root.get(DESCRIPTION.getFieldName())), "%" + description.toLowerCase() + "%"));
        }
        return predicates.toArray(new Predicate[0]);
    }

    @Override
    public Page<Transaction> findTransactionsByCriteria(String accountName, BigDecimal minAmount, BigDecimal maxAmount,
                                                        LocalDate fromDate, LocalDate toDate, String category,
                                                        String description, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Transaction> cq = cb.createQuery(Transaction.class);
        Root<Transaction> root = cq.from(Transaction.class);
        Predicate[] predicates = buildPredicates(cb, root, accountName, minAmount, maxAmount, fromDate, toDate, category, description);
        cq.where(predicates);

        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    orders.add(cb.asc(root.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(root.get(order.getProperty())));
                }
            });
            cq.orderBy(orders);
        }

        TypedQuery<Transaction> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Transaction> resultList = query.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Transaction> countRoot = countQuery.from(Transaction.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, accountName, minAmount, maxAmount, fromDate, toDate, category, description));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, total);
    }

    @Override
    public BigDecimal calculateTotalBalanceByCriteria(String accountName, BigDecimal minAmount, BigDecimal maxAmount,
                                                      LocalDate fromDate, LocalDate toDate, String category,
                                                      String description) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> cq = cb.createQuery(BigDecimal.class);
        Root<Transaction> root = cq.from(Transaction.class);
        Predicate[] predicates = buildPredicates(cb, root, accountName, minAmount, maxAmount, fromDate, toDate, category, description);
        cq.select(cb.coalesce(cb.sum(root.get(AMOUNT.getFieldName())), BigDecimal.ZERO)).where(predicates);
        return entityManager.createQuery(cq).getSingleResult();
    }
}
