package com.finance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度2-50")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;

    @Size(max = 1400000, message = "头像图片过大")
    private String avatar;
}
