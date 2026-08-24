package com.finance.controller;

import com.finance.common.Result;
import com.finance.dto.TransactionRequest;
import com.finance.entity.Transaction;
import com.finance.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/**
 * 交易流水接口
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    /**
     * 分页查询交易流水
     */
    @GetMapping
    public Result<?> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(transactionService.getTransactions(
                startDate, endDate, categoryId, type, keyword, page, size));
    }

    /**
     * 新增交易
     */
    @PostMapping
    public Result<Transaction> create(@Valid @RequestBody TransactionRequest request) {
        return Result.success(transactionService.createTransaction(request));
    }

    /**
     * 编辑交易
     */
    @PutMapping("/{id}")
    public Result<Transaction> update(@PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
        return Result.success(transactionService.updateTransaction(id, request));
    }

    /**
     * 删除交易
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return Result.success();
    }
}
