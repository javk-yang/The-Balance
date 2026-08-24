package com.finance.controller;

import com.finance.common.SecurityUtils;
import com.finance.entity.Category;
import com.finance.entity.Transaction;
import com.finance.repository.CategoryRepository;
import com.finance.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据导出接口 - CSV/Excel
 */
@RestController
@RequestMapping("/api/export")
public class ExportController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * 导出 Excel
     */
    @GetMapping("/excel")
    public void exportExcel(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            HttpServletResponse response) throws IOException {

        Long userId = SecurityUtils.getCurrentUserId();
        if (startDate == null) startDate = LocalDate.now().withDayOfYear(1);
        if (endDate == null) endDate = LocalDate.now();

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, startDate, endDate);

        // 构建分类映射
        List<Category> categories = categoryRepository.findByUserIdOrderBySortOrderAsc(userId);
        Map<Long, Category> categoryMap = new HashMap<>();
        for (Category c : categories) {
            categoryMap.put(c.getId(), c);
        }

        // 创建 Excel 工作簿
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("交易流水");

            // 表头
            Row header = sheet.createRow(0);
            String[] headers = {"日期", "类型", "分类", "金额", "备注"};
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 数据行
            int rowIdx = 1;
            for (Transaction t : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(t.getDate().toString());
                row.createCell(1).setCellValue("INCOME".equals(t.getType()) ? "收入" : "支出");
                Category cat = categoryMap.get(t.getCategoryId());
                row.createCell(2).setCellValue(cat != null ? cat.getName() : "未知");
                row.createCell(3).setCellValue(t.getAmount().doubleValue());
                row.createCell(4).setCellValue(t.getNote() != null ? t.getNote() : "");
            }

            // 自动列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 输出
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=transactions.xlsx");
            workbook.write(response.getOutputStream());
        }
    }
}
