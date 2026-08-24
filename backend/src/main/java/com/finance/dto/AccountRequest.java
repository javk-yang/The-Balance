package com.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AccountRequest {
    @NotBlank(message = "账户名称不能为空")
    @Size(max = 50)
    private String name;

    @NotBlank(message = "账户类型不能为空")
    private String type; // CASH, BANK_CARD, ALIPAY, WECHAT

    @DecimalMin(value = "0", message = "余额不能为负")
    private BigDecimal balance;

    private String remark;
}
