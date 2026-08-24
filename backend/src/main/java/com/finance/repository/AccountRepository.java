package com.finance.repository;

import com.finance.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserIdOrderByCreatedAtDesc(Long userId);
}
