package com.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AssetRequest {
    @NotBlank(message = "资产名称不能为空")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "资产类别不能为空")
    @Size(max = 50)
    private String category;

    @NotNull(message = "购入价格不能为空")
    @DecimalMin(value = "0", message = "购入价格不能为负")
    private BigDecimal purchasePrice;

    @DecimalMin(value = "0", message = "当前价值不能为负")
    private BigDecimal currentValue;

    private Boolean liquidatable;

    private String status;

    private LocalDate purchaseDate;

    @Size(max = 500)
    private String remark;
}
