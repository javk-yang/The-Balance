package com.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BudgetRequest {
    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "预算金额必须大于0")
    private BigDecimal amount;

    @NotBlank(message = "月份不能为空")
    private String month; // yyyy-MM
}
