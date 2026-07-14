package com.example.admin_service.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WalletTransactionTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        WalletTransaction transaction = new WalletTransaction();

        LocalDateTime now = LocalDateTime.now();

        transaction.setTransactionId("TXN001");
        transaction.setUserId("USER001");
        transaction.setTransactionType("CREDIT");
        transaction.setAmount(new BigDecimal("1000.00"));
        transaction.setDescription("Wallet Recharge");
        transaction.setReferenceId("REF001");
        transaction.setRazorpayPaymentId("PAY001");
        transaction.setRazorpayOrderId("ORD001");
        transaction.setStatus("SUCCESS");
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);
        transaction.setBalanceBefore(new BigDecimal("500.00"));
        transaction.setBalanceAfter(new BigDecimal("1500.00"));

        assertEquals("TXN001", transaction.getTransactionId());
        assertEquals("USER001", transaction.getUserId());
        assertEquals("CREDIT", transaction.getTransactionType());
        assertEquals(new BigDecimal("1000.00"), transaction.getAmount());
        assertEquals("Wallet Recharge", transaction.getDescription());
        assertEquals("REF001", transaction.getReferenceId());
        assertEquals("PAY001", transaction.getRazorpayPaymentId());
        assertEquals("ORD001", transaction.getRazorpayOrderId());
        assertEquals("SUCCESS", transaction.getStatus());
        assertEquals(now, transaction.getCreatedAt());
        assertEquals(now, transaction.getUpdatedAt());
        assertEquals(new BigDecimal("500.00"), transaction.getBalanceBefore());
        assertEquals(new BigDecimal("1500.00"), transaction.getBalanceAfter());
    }

    @Test
    void testBuilder() {
        LocalDateTime now = LocalDateTime.now();

        WalletTransaction transaction = WalletTransaction.builder()
                .transactionId("TXN001")
                .userId("USER001")
                .transactionType("DEBIT")
                .amount(new BigDecimal("250.00"))
                .description("Purchase")
                .referenceId("REF123")
                .razorpayPaymentId("PAY123")
                .razorpayOrderId("ORD123")
                .status("SUCCESS")
                .createdAt(now)
                .updatedAt(now)
                .balanceBefore(new BigDecimal("1000.00"))
                .balanceAfter(new BigDecimal("750.00"))
                .build();

        assertEquals("TXN001", transaction.getTransactionId());
        assertEquals("USER001", transaction.getUserId());
        assertEquals("DEBIT", transaction.getTransactionType());
        assertEquals(new BigDecimal("250.00"), transaction.getAmount());
        assertEquals("Purchase", transaction.getDescription());
        assertEquals("REF123", transaction.getReferenceId());
        assertEquals("PAY123", transaction.getRazorpayPaymentId());
        assertEquals("ORD123", transaction.getRazorpayOrderId());
        assertEquals("SUCCESS", transaction.getStatus());
        assertEquals(now, transaction.getCreatedAt());
        assertEquals(now, transaction.getUpdatedAt());
        assertEquals(new BigDecimal("1000.00"), transaction.getBalanceBefore());
        assertEquals(new BigDecimal("750.00"), transaction.getBalanceAfter());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();

        WalletTransaction transaction = new WalletTransaction(
                "TXN001",
                "USER001",
                "CREDIT",
                new BigDecimal("500.00"),
                "Refund",
                "REF999",
                "PAY999",
                "ORD999",
                "SUCCESS",
                now,
                now,
                new BigDecimal("200.00"),
                new BigDecimal("700.00")
        );

        assertEquals("TXN001", transaction.getTransactionId());
        assertEquals("USER001", transaction.getUserId());
        assertEquals("CREDIT", transaction.getTransactionType());
        assertEquals(new BigDecimal("500.00"), transaction.getAmount());
    }

    @Test
    void testEqualsHashCodeAndToString() {
        WalletTransaction t1 = new WalletTransaction();
        t1.setTransactionId("TXN001");

        WalletTransaction t2 = new WalletTransaction();
        t2.setTransactionId("TXN001");

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
        assertTrue(t1.toString().contains("TXN001"));
    }
}