package com.finance.service;

import com.finance.common.SecurityUtils;
import com.finance.entity.Account;
import com.finance.entity.Asset;
import com.finance.entity.Category;
import com.finance.entity.Loan;
import com.finance.entity.LoanPayment;
import com.finance.entity.Project;
import com.finance.repository.AccountRepository;
import com.finance.repository.AssetRepository;
import com.finance.repository.CategoryRepository;
import com.finance.repository.LoanPaymentRepository;
import com.finance.repository.LoanRepository;
import com.finance.repository.ProjectRepository;
import com.finance.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * 看板服务 - 统计汇总数据
 */
@Service
public class DashboardService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanPaymentRepository loanPaymentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * 获取看板汇总数据
     */
    public Map<String, Object> getSummary() {
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());

        // 本月收入
        BigDecimal income = transactionRepository.sumByTypeAndDateRange(userId, "INCOME", monthStart, monthEnd);
        // 本月支出
        BigDecimal expense = transactionRepository.sumByTypeAndDateRange(userId, "EXPENSE", monthStart, monthEnd);

        Map<String, Object> summary = new HashMap<>();
        summary.put("monthIncome", income != null ? income : BigDecimal.ZERO);
        summary.put("monthExpense", expense != null ? expense : BigDecimal.ZERO);
        summary.put("monthBalance", (income != null ? income : BigDecimal.ZERO)
                .subtract(expense != null ? expense : BigDecimal.ZERO));

        // 总资产、负债、现金流与净资产
        List<Account> accounts = accountRepository.findByUserIdOrderByCreatedAtDesc(userId);
        BigDecimal availableCash = BigDecimal.ZERO;
        BigDecimal totalDebt = BigDecimal.ZERO;
        for (Account account : accounts) {
            BigDecimal balance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
            if (balance.compareTo(BigDecimal.ZERO) >= 0) {
                availableCash = availableCash.add(balance);
            } else {
                totalDebt = totalDebt.add(balance.abs());
            }
        }
        BigDecimal fixedAssetValue = BigDecimal.ZERO;
        BigDecimal liquidatableAssetValue = BigDecimal.ZERO;
        int assetCount = 0;
        for (Asset asset : assetRepository.findByUserIdOrderByCreatedAtDesc(userId)) {
            if (!("ACTIVE".equalsIgnoreCase(asset.getStatus()) || "HOLDING".equalsIgnoreCase(asset.getStatus()))) continue;
            BigDecimal currentValue = asset.getCurrentValue() != null ? asset.getCurrentValue() : BigDecimal.ZERO;
            fixedAssetValue = fixedAssetValue.add(currentValue);
            if (Boolean.TRUE.equals(asset.getLiquidatable())) {
                liquidatableAssetValue = liquidatableAssetValue.add(currentValue);
            }
            assetCount++;
        }
        BigDecimal totalAssets = availableCash.add(fixedAssetValue);
        summary.put("totalAssets", totalAssets);
        summary.put("availableCash", availableCash);
        summary.put("fixedAssetValue", fixedAssetValue);
        summary.put("liquidatableAssetValue", liquidatableAssetValue);
        summary.put("assetCount", assetCount);
        summary.put("cashAtMonthStart", availableCash.subtract((income != null ? income : BigDecimal.ZERO)
                .subtract(expense != null ? expense : BigDecimal.ZERO)));
        summary.put("totalDebt", totalDebt);
        summary.put("netWorth", totalAssets.subtract(totalDebt));
        summary.put("cashFlow", (income != null ? income : BigDecimal.ZERO)
                .subtract(expense != null ? expense : BigDecimal.ZERO));

        // 已签约项目回款汇总。合同总额不计入账户资产，只有已收款指标单独展示。
        List<Project> projects = projectRepository.findByUserIdOrderByCreatedAtDesc(userId);
        BigDecimal projectContractValue = BigDecimal.ZERO;
        BigDecimal projectReceived = BigDecimal.ZERO;
        BigDecimal projectPending = BigDecimal.ZERO;
        BigDecimal projectCurrentMonthReceived = BigDecimal.ZERO;
        int activeProjectCount = 0;
        YearMonth currentProjectMonth = YearMonth.from(now);
        for (Project project : projects) {
            project.calculateStats();
            projectContractValue = projectContractValue.add(project.getPrice() == null ? BigDecimal.ZERO : project.getPrice());
            projectReceived = projectReceived.add(project.getTotalReceived() == null ? BigDecimal.ZERO : project.getTotalReceived());
            projectPending = projectPending.add(project.getPendingAmount() == null ? BigDecimal.ZERO : project.getPendingAmount());
            if ("ACTIVE".equalsIgnoreCase(project.getStatus())) activeProjectCount++;
            if (project.getContractDate() != null && YearMonth.from(project.getContractDate()).equals(currentProjectMonth)) {
                projectCurrentMonthReceived = projectCurrentMonthReceived.add(project.getTotalReceived() == null ? BigDecimal.ZERO : project.getTotalReceived());
            }
        }
        summary.put("projectContractValue", projectContractValue);
        summary.put("projectReceived", projectReceived);
        summary.put("projectPending", projectPending);
        summary.put("projectCurrentMonthReceived", projectCurrentMonthReceived);
        summary.put("projectCount", projects.size());
        summary.put("activeProjectCount", activeProjectCount);

        // 贷款负债与还款压力
        List<Loan> loans = loanRepository.findByUserIdOrderByCreatedAtDesc(userId);
        BigDecimal loanTotalBorrowed = BigDecimal.ZERO;
        BigDecimal loanRemainingPrincipal = BigDecimal.ZERO;
        BigDecimal loanPaidInterest = BigDecimal.ZERO;
        BigDecimal loanMonthlyDue = BigDecimal.ZERO;
        BigDecimal loanMonthlyPaid = BigDecimal.ZERO;
        BigDecimal loanPaidPrincipal = BigDecimal.ZERO;
        List<Map<String, Object>> loanSummary = new ArrayList<>();
        YearMonth currentMonth = YearMonth.from(now);
        for (Loan loan : loans) {
            List<LoanPayment> payments = loanPaymentRepository
                    .findByLoanIdAndUserIdOrderByPaymentDateDesc(loan.getId(), userId);
            loan.calculateStats(payments);
            BigDecimal principal = loan.getPrincipal() != null ? loan.getPrincipal() : BigDecimal.ZERO;
            BigDecimal remainingPrincipal = loan.getRemainingPrincipal() != null
                    ? loan.getRemainingPrincipal() : principal;
            BigDecimal paidPrincipal = loan.getPaidPrincipal() != null
                    ? loan.getPaidPrincipal() : BigDecimal.ZERO;
            BigDecimal paidInterest = loan.getPaidInterest() != null
                    ? loan.getPaidInterest() : BigDecimal.ZERO;
            loanTotalBorrowed = loanTotalBorrowed.add(principal);
            loanRemainingPrincipal = loanRemainingPrincipal.add(remainingPrincipal);
            loanPaidPrincipal = loanPaidPrincipal.add(paidPrincipal);
            loanPaidInterest = loanPaidInterest.add(paidInterest);
            if (!"CLOSED".equalsIgnoreCase(loan.getStatus()) && !"PAID".equalsIgnoreCase(loan.getStatus())) {
                loanMonthlyDue = loanMonthlyDue.add(loan.getMonthlyPayment() != null
                        ? loan.getMonthlyPayment() : BigDecimal.ZERO);
            }
            BigDecimal currentLoanPaid = payments.stream()
                    .filter(p -> p.getPaymentDate() != null && YearMonth.from(p.getPaymentDate()).equals(currentMonth))
                    .map(LoanPayment::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            loanMonthlyPaid = loanMonthlyPaid.add(currentLoanPaid);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", loan.getId());
            item.put("name", loan.getName());
            item.put("remainingPrincipal", remainingPrincipal);
            item.put("monthlyPayment", loan.getMonthlyPayment() != null ? loan.getMonthlyPayment() : BigDecimal.ZERO);
            item.put("progress", loan.getProgress() != null ? loan.getProgress() : BigDecimal.ZERO);
            loanSummary.add(item);
        }
        summary.put("loanTotalBorrowed", loanTotalBorrowed);
        summary.put("loanRemainingPrincipal", loanRemainingPrincipal);
        summary.put("loanPaidPrincipal", loanPaidPrincipal);
        summary.put("loanPaidInterest", loanPaidInterest);
        summary.put("loanMonthlyDue", loanMonthlyDue);
        summary.put("loanMonthlyPaid", loanMonthlyPaid);
        summary.put("loanCount", loans.size());
        summary.put("loanProgress", loanTotalBorrowed.signum() == 0 ? BigDecimal.ZERO
                : loanPaidPrincipal.multiply(BigDecimal.valueOf(100))
                .divide(loanTotalBorrowed, 2, java.math.RoundingMode.HALF_UP));
        summary.put("loanSummary", loanSummary);
        // 贷款剩余本金属于负债，纳入总负债和净资产
        summary.put("totalDebt", totalDebt.add(loanRemainingPrincipal));
        summary.put("netWorth", totalAssets.subtract(totalDebt).subtract(loanRemainingPrincipal));

        // 近 7 天收支趋势
        LocalDate weekStart = now.minusDays(6);
        List<Object[]> dailyData = transactionRepository.dailySumByType(userId, weekStart, now);
        summary.put("weeklyTrend", formatDailyTrend(dailyData, weekStart, now));

        // 支出类别占比
        List<Object[]> categoryData = transactionRepository.sumExpenseByCategory(userId, monthStart, monthEnd);
        summary.put("expenseByCategory", formatCategoryData(categoryData, userId));

        // 最近交易
        summary.put("recentTransactions", transactionRepository.findRecentTransactions(userId, 5));

        return summary;
    }

    /**
     * 格式化每日趋势数据
     */
    private List<Map<String, Object>> formatDailyTrend(List<Object[]> data, LocalDate start, LocalDate end) {
        Map<LocalDate, Map<String, BigDecimal>> map = new LinkedHashMap<>();
        for (int i = 0; i <= 6; i++) {
            LocalDate date = start.plusDays(i);
            map.put(date, new HashMap<>());
            map.get(date).put("INCOME", BigDecimal.ZERO);
            map.get(date).put("EXPENSE", BigDecimal.ZERO);
        }

        for (Object[] row : data) {
            LocalDate date = (LocalDate) row[0];
            String type = (String) row[1];
            BigDecimal amount = (BigDecimal) row[2];
            if (map.containsKey(date)) {
                map.get(date).put(type, amount);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<LocalDate, Map<String, BigDecimal>> entry : map.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey().toString());
            item.put("income", entry.getValue().get("INCOME"));
            item.put("expense", entry.getValue().get("EXPENSE"));
            result.add(item);
        }
        return result;
    }

    /**
     * 格式化分类支出数据
     */
    private List<Map<String, Object>> formatCategoryData(List<Object[]> data, Long userId) {
        List<Category> categories = categoryRepository.findByUserIdOrderBySortOrderAsc(userId);
        Map<Long, String> categoryMap = new HashMap<>();
        Map<Long, String> iconMap = new HashMap<>();
        Map<Long, String> colorMap = new HashMap<>();
        for (Category c : categories) {
            categoryMap.put(c.getId(), c.getName());
            iconMap.put(c.getId(), c.getIcon());
            colorMap.put(c.getId(), c.getColor());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : data) {
            Long categoryId = (Long) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            Map<String, Object> item = new HashMap<>();
            item.put("categoryId", categoryId);
            item.put("name", categoryMap.getOrDefault(categoryId, "未知"));
            item.put("icon", iconMap.getOrDefault(categoryId, ""));
            item.put("color", colorMap.getOrDefault(categoryId, "#6366f1"));
            item.put("amount", amount);
            result.add(item);
        }
        return result;
    }
}
