package com.finance.controller;

import com.finance.common.Result;
import com.finance.dto.BudgetRequest;
import com.finance.entity.Budget;
import com.finance.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 预算接口
 */
@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    /**
     * 查询某月预算（含使用进度）
     */
    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam String month) {
        return Result.success(budgetService.getBudgetsByMonth(month));
    }

    /**
     * 设置预算
     */
    @PostMapping
    public Result<Budget> set(@Valid @RequestBody BudgetRequest request) {
        return Result.success(budgetService.setBudget(request));
    }

    /**
     * 删除预算
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return Result.success();
    }
}
