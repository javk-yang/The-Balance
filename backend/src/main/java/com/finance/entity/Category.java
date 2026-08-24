package com.finance.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 消费分类（消费板块）实体
 * 支持自定义新增/编辑/删除消费类别
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 分类名称: 餐饮、购物、工资等 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 类型: INCOME-收入, EXPENSE-支出 */
    @Column(nullable = false, length = 10)
    private String type;

    /** 图标名称（前端图标库映射） */
    @Column(length = 50)
    private String icon;

    /** 主题色 */
    @Column(length = 20)
    private String color;

    /** 父分类ID，支持二级分类 */
    @Column(name = "parent_id")
    private Long parentId;

    /** 排序 */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private Integer status = 1;

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
