package com.finance.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 安全工具类 - 从 SecurityContext 中获取当前用户 ID
 */
@Component
public class SecurityUtils {

    /**
     * 获取当前登录用户 ID
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            return (Long) auth.getPrincipal();
        }
        throw new RuntimeException("用户未登录");
    }
}
