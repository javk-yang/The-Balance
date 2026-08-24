package com.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称最长50")
    private String name;

    @NotBlank(message = "类型不能为空")
    private String type; // INCOME / EXPENSE

    private String icon;
    private String color;
    private Long parentId;
    private Integer sortOrder;
}
