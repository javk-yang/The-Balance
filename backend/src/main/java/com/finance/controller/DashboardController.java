package com.finance.controller;

import com.finance.common.Result;
import com.finance.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * 看板统计接口
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取看板汇总数据
     */
    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        return Result.success(dashboardService.getSummary());
    }
}
