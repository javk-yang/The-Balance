package com.finance.service;

import com.finance.common.SecurityUtils;
import com.finance.dto.TransactionRequest;
import com.finance.entity.Account;
import com.finance.entity.Transaction;
import com.finance.repository.AccountRepository;
import com.finance.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 交易流水服务 - 记账 CRUD
 */
@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * 分页查询交易流水
     */
    public Page<Transaction> getTransactions(
            LocalDate startDate, LocalDate endDate,
            Long categoryId, String type, String keyword,
            int page, int size) {

        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findByFilters(
                userId, startDate, endDate, categoryId, type, keyword, pageable);
    }

    /**
     * 新增交易
     */
    @Transactional
    public Transaction createTransaction(TransactionRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Transaction t = new Transaction();
        t.setUserId(userId);
        t.setAccountId(request.getAccountId());
        t.setCategoryId(request.getCategoryId());
        t.setAmount(request.getAmount());
        t.setType(request.getType());
        t.setDate(request.getDate());
        t.setNote(request.getNote());

        Transaction saved = transactionRepository.save(t);

        // 更新账户余额
        accountRepository.findById(request.getAccountId()).ifPresent(account -> {
            BigDecimal delta = "INCOME".equals(request.getType()) ?
                    request.getAmount() : request.getAmount().negate();
            account.setBalance(account.getBalance().add(delta));
            accountRepository.save(account);
        });

        return saved;
    }

    /**
     * 编辑交易
     */
    @Transactional
    public Transaction updateTransaction(Long id, TransactionRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Transaction t = transactionRepository.findById(id)
                .filter(tx -> tx.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("交易记录不存在"));

        // 先回滚旧记录对账户余额的影响
        accountRepository.findById(t.getAccountId()).ifPresent(account -> {
            BigDecimal oldDelta = "INCOME".equals(t.getType()) ?
                    t.getAmount().negate() : t.getAmount();
            account.setBalance(account.getBalance().add(oldDelta));
            accountRepository.save(account);
        });

        // 应用新记录
        t.setAccountId(request.getAccountId());
        t.setCategoryId(request.getCategoryId());
        t.setAmount(request.getAmount());
        t.setType(request.getType());
        t.setDate(request.getDate());
        t.setNote(request.getNote());
        Transaction saved = transactionRepository.save(t);

        // 更新账户余额
        accountRepository.findById(request.getAccountId()).ifPresent(account -> {
            BigDecimal delta = "INCOME".equals(request.getType()) ?
                    request.getAmount() : request.getAmount().negate();
            account.setBalance(account.getBalance().add(delta));
            accountRepository.save(account);
        });

        return saved;
    }

    /**
     * 删除交易
     */
    @Transactional
    public void deleteTransaction(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Transaction t = transactionRepository.findById(id)
                .filter(tx -> tx.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("交易记录不存在"));

        // 回滚账户余额
        accountRepository.findById(t.getAccountId()).ifPresent(account -> {
            BigDecimal delta = "INCOME".equals(t.getType()) ?
                    t.getAmount().negate() : t.getAmount();
            account.setBalance(account.getBalance().add(delta));
            accountRepository.save(account);
        });

        transactionRepository.delete(t);
    }
}
