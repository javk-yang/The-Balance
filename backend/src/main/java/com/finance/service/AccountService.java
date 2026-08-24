package com.finance.service;

import com.finance.common.SecurityUtils;
import com.finance.dto.AccountRequest;
import com.finance.entity.Account;
import com.finance.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

/**
 * 账户服务
 */
@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    /**
     * 查询当前用户所有账户
     */
    public List<Account> getAllAccounts() {
        return accountRepository.findByUserIdOrderByCreatedAtDesc(SecurityUtils.getCurrentUserId());
    }

    /**
     * 新增账户
     */
    public Account createAccount(AccountRequest request) {
        Account account = new Account();
        account.setUserId(SecurityUtils.getCurrentUserId());
        account.setName(request.getName());
        account.setType(request.getType());
        account.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);
        account.setRemark(request.getRemark());
        return accountRepository.save(account);
    }

    /**
     * 编辑账户
     */
    public Account updateAccount(Long id, AccountRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Account account = accountRepository.findById(id)
                .filter(a -> a.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("账户不存在"));

        account.setName(request.getName());
        account.setType(request.getType());
        if (request.getBalance() != null) account.setBalance(request.getBalance());
        if (request.getRemark() != null) account.setRemark(request.getRemark());

        return accountRepository.save(account);
    }

    /**
     * 删除账户
     */
    public void deleteAccount(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Account account = accountRepository.findById(id)
                .filter(a -> a.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("账户不存在"));
        accountRepository.delete(account);
    }
}
