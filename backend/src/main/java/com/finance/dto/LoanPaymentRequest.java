package com.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanPaymentRequest {
    @NotNull(message = "还款金额不能为空")
    @DecimalMin(value = "0.01", message = "还款金额必须大于0")
    private BigDecimal amount;
    private LocalDate paymentDate;
    @DecimalMin(value = "0", message = "本金金额不能为负")
    private BigDecimal principalAmount;
    @DecimalMin(value = "0", message = "利息金额不能为负")
    private BigDecimal interestAmount;
    @Size(max = 500)
    private String note;
}
