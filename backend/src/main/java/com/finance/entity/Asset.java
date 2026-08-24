package com.finance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 固定资产实体。 */
@Entity
@Table(name = "assets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "purchase_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @Column(name = "current_value", nullable = false, precision = 14, scale = 2)
    private BigDecimal currentValue = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean liquidatable = Boolean.TRUE;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(length = 500)
    private String remark;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (purchasePrice == null) purchasePrice = BigDecimal.ZERO;
        if (currentValue == null) currentValue = purchasePrice;
        if (liquidatable == null) liquidatable = Boolean.TRUE;
        if (status == null || status.isBlank()) status = "ACTIVE";
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
