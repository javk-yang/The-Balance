package com.finance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 已签约项目实体。 */
@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String client;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal price;

    @Column(name = "deposit_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @Column(name = "deposit_status", nullable = false, length = 20)
    private String depositStatus = "UNPAID";

    @Column(name = "balance_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal balanceAmount = BigDecimal.ZERO;

    @Column(name = "balance_status", nullable = false, length = 20)
    private String balanceStatus = "UNPAID";

    @Column(name = "contract_date")
    private LocalDate contractDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(length = 500)
    private String remark;

    @Transient
    private BigDecimal totalReceived;

    @Transient
    private BigDecimal pendingAmount;

    @Transient
    private BigDecimal paymentProgress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (depositAmount == null) depositAmount = BigDecimal.ZERO;
        if (balanceAmount == null) balanceAmount = BigDecimal.ZERO;
        if (depositStatus == null || depositStatus.isBlank()) depositStatus = "UNPAID";
        if (balanceStatus == null || balanceStatus.isBlank()) balanceStatus = "UNPAID";
        if (status == null || status.isBlank()) status = "ACTIVE";
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /** 按各阶段收款状态计算已收、待收和进度。 */
    public void calculateStats() {
        BigDecimal total = price == null ? BigDecimal.ZERO : price;
        BigDecimal deposit = depositAmount == null ? BigDecimal.ZERO : depositAmount;
        BigDecimal balance = balanceAmount == null ? BigDecimal.ZERO : balanceAmount;
        BigDecimal received = BigDecimal.ZERO;
        if (!"UNPAID".equalsIgnoreCase(depositStatus)) received = received.add(deposit);
        if (!"UNPAID".equalsIgnoreCase(balanceStatus)) received = received.add(balance);
        received = received.min(total).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        this.totalReceived = received;
        this.pendingAmount = total.subtract(received).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        this.paymentProgress = total.signum() == 0 ? BigDecimal.ZERO
                : received.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP).min(BigDecimal.valueOf(100));
    }
}
