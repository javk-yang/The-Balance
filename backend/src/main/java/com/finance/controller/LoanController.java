package com.finance.controller;

import com.finance.common.Result;
import com.finance.dto.LoanPaymentRequest;
import com.finance.dto.LoanRequest;
import com.finance.entity.Loan;
import com.finance.entity.LoanPayment;
import com.finance.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
    @Autowired
    private LoanService loanService;

    @GetMapping
    public Result<List<Loan>> list() {
        return Result.success(loanService.getAllLoans());
    }

    @GetMapping("/overview")
    public Result<Map<String, BigDecimal>> overview() {
        return Result.success(loanService.getOverview());
    }

    @GetMapping("/{id}")
    public Result<Loan> detail(@PathVariable Long id) {
        return Result.success(loanService.getLoan(id));
    }

    @PostMapping
    public Result<Loan> create(@Valid @RequestBody LoanRequest request) {
        return Result.success(loanService.createLoan(request));
    }

    @PutMapping("/{id}")
    public Result<Loan> update(@PathVariable Long id, @Valid @RequestBody LoanRequest request) {
        return Result.success(loanService.updateLoan(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return Result.success();
    }

    @GetMapping("/{id}/payments")
    public Result<List<LoanPayment>> payments(@PathVariable Long id) {
        return Result.success(loanService.getPayments(id));
    }

    @PostMapping("/{id}/payments")
    public Result<LoanPayment> addPayment(@PathVariable Long id, @Valid @RequestBody LoanPaymentRequest request) {
        return Result.success(loanService.addPayment(id, request));
    }

    @DeleteMapping("/{id}/payments/{paymentId}")
    public Result<Void> deletePayment(@PathVariable Long id, @PathVariable Long paymentId) {
        loanService.deletePayment(id, paymentId);
        return Result.success();
    }
}
