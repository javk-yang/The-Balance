package com.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProjectRequest {
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String client;

    @NotNull(message = "项目金额不能为空")
    @DecimalMin(value = "0", message = "项目金额不能为负")
    private BigDecimal price;

    @DecimalMin(value = "0", message = "定金金额不能为负")
    private BigDecimal depositAmount;

    private String depositStatus;

    @DecimalMin(value = "0", message = "尾款金额不能为负")
    private BigDecimal balanceAmount;

    private String balanceStatus;
    private LocalDate contractDate;
    private LocalDate dueDate;
    private String status;

    @Size(max = 500)
    private String remark;
}
