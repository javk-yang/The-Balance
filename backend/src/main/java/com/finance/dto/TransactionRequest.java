package com.finance.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {
    @NotNull(message = "账户不能为空")
    private Long accountId;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    @NotBlank(message = "类型不能为空")
    private String type; // INCOME / EXPENSE

    @NotNull(message = "日期不能为空")
    private LocalDate date;

    private String note;
}
