package com.finance.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 贷款实体 */
@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String lender;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal principal;

    @Column(name = "annual_rate", precision = 8, scale = 4)
    private BigDecimal annualRate;

    @Column(name = "term_months")
    private Integer termMonths;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "payment_day")
    private Integer paymentDay;

    @Column(name = "monthly_payment", precision = 14, scale = 2)
    private BigDecimal monthlyPayment;

    @Column(length = 20, nullable = false)
    private String status = "ACTIVE";

    @Column(length = 500)
    private String remark;

    @Transient
    private BigDecimal remainingPrincipal;

    @Transient
    private BigDecimal paidPrincipal;

    @Transient
    private BigDecimal paidInterest;

    @Transient
    private BigDecimal progress;

    @JsonIgnore
    @Transient
    private List<LoanPayment> paymentRecords = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (status == null || status.isBlank()) status = "ACTIVE";
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void calculateStats(List<LoanPayment> payments) {
        this.paymentRecords = payments == null ? new ArrayList<>() : payments;
        BigDecimal paidPrincipal = this.paymentRecords.stream()
                .map(p -> p.getPrincipalAmount() != null ? p.getPrincipalAmount() : p.getAmount())
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paidInterest = this.paymentRecords.stream()
                .map(LoanPayment::getInterestAmount)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.paidPrincipal = paidPrincipal.setScale(2, RoundingMode.HALF_UP);
        this.paidInterest = paidInterest.setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = principal == null ? BigDecimal.ZERO : principal;
        this.remainingPrincipal = total.subtract(paidPrincipal).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        this.progress = total.signum() == 0 ? BigDecimal.ZERO : paidPrincipal.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP).min(BigDecimal.valueOf(100));
    }
}
