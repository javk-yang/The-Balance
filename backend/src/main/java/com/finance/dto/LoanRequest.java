package com.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanRequest {
    @NotBlank(message = "贷款名称不能为空")
    @Size(max = 100)
    private String name;
    @Size(max = 100)
    private String lender;
    @NotBlank(message = "贷款类型不能为空")
    private String type;
    @NotNull(message = "本金不能为空")
    @DecimalMin(value = "0.01", message = "本金必须大于0")
    private BigDecimal principal;
    @DecimalMin(value = "0", message = "年利率不能为负")
    private BigDecimal annualRate;
    @Min(value = 1, message = "期限必须大于0")
    private Integer termMonths;
    private LocalDate startDate;
    @Min(value = 1, message = "还款日必须在1到31之间")
    @Max(value = 31, message = "还款日必须在1到31之间")
    private Integer paymentDay;
    @DecimalMin(value = "0", message = "月供不能为负")
    private BigDecimal monthlyPayment;
    private String status;
    @Size(max = 500)
    private String remark;
}
