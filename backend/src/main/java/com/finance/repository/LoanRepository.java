package com.finance.repository;

import com.finance.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserIdOrderByCreatedAtDesc(Long userId);
}
