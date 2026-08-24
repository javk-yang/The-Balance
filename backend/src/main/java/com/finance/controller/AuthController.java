package com.finance.controller;

import com.finance.common.Result;
import com.finance.dto.LoginRequest;
import com.finance.dto.RegisterRequest;
import com.finance.service.AuthService;
import com.finance.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        Map<String, Object> result = authService.register(request);
        // 注册成功后初始化默认消费分类
        Long userId = (Long) result.get("userId");
        categoryService.initDefaultCategories(userId);
        return Result.success(result);
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }
}
