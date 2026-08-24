package com.finance.controller;

import com.finance.common.Result;
import com.finance.dto.AccountRequest;
import com.finance.entity.Account;
import com.finance.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 账户接口
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public Result<List<Account>> list() {
        return Result.success(accountService.getAllAccounts());
    }

    @PostMapping
    public Result<Account> create(@Valid @RequestBody AccountRequest request) {
        return Result.success(accountService.createAccount(request));
    }

    @PutMapping("/{id}")
    public Result<Account> update(@PathVariable Long id, @Valid @RequestBody AccountRequest request) {
        return Result.success(accountService.updateAccount(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return Result.success();
    }
}
