package com.finance.service;

import com.finance.common.SecurityUtils;
import com.finance.dto.BudgetRequest;
import com.finance.entity.Budget;
import com.finance.repository.BudgetRepository;
import com.finance.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预算服务
 */
@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * 查询某月预算列表（带使用进度）
     */
    public List<Map<String, Object>> getBudgetsByMonth(String month) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Budget> budgets = budgetRepository.findByUserIdAndMonth(userId, month);

        // 计算月份的起止日期
        YearMonth ym = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Budget b : budgets) {
            BigDecimal spent = transactionRepository.sumExpenseByCategoryAndDateRange(
                    userId, b.getCategoryId(), startDate, endDate);

            Map<String, Object> item = new HashMap<>();
            item.put("id", b.getId());
            item.put("categoryId", b.getCategoryId());
            item.put("amount", b.getAmount());
            item.put("month", b.getMonth());
            item.put("spent", spent != null ? spent : BigDecimal.ZERO);
            item.put("remaining", b.getAmount().subtract(spent != null ? spent : BigDecimal.ZERO));
            // 是否超支
            item.put("overBudget", (spent != null && spent.compareTo(b.getAmount()) > 0));
            result.add(item);
        }
        return result;
    }

    /**
     * 设置预算（新增或更新）
     */
    public Budget setBudget(BudgetRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Budget existing = budgetRepository
                .findByUserIdAndCategoryIdAndMonth(userId, request.getCategoryId(), request.getMonth())
                .orElse(null);

        if (existing != null) {
            existing.setAmount(request.getAmount());
            return budgetRepository.save(existing);
        }

        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setCategoryId(request.getCategoryId());
        budget.setAmount(request.getAmount());
        budget.setMonth(request.getMonth());
        return budgetRepository.save(budget);
    }

    /**
     * 删除预算
     */
    public void deleteBudget(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Budget budget = budgetRepository.findById(id)
                .filter(b -> b.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("预算不存在"));
        budgetRepository.delete(budget);
    }
}
