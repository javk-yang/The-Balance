package com.finance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 贷款还款记录 */
@Entity
@Table(name = "loan_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "loan_id", nullable = false)
    private Long loanId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "principal_amount", precision = 14, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "interest_amount", precision = 14, scale = 2)
    private BigDecimal interestAmount;

    @Column(length = 500)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (paymentDate == null) paymentDate = LocalDate.now();
        if (principalAmount == null) principalAmount = amount;
        if (interestAmount == null) interestAmount = BigDecimal.ZERO;
        createdAt = LocalDateTime.now();
    }
}
