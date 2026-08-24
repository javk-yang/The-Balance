package com.finance.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 交易流水实体（收入/支出记录）
 */
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /** 金额（正数） */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** 类型: INCOME-收入, EXPENSE-支出 */
    @Column(nullable = false, length = 10)
    private String type;

    /** 交易日期 */
    @Column(nullable = false)
    private LocalDate date;

    /** 备注 */
    @Column(length = 500)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
