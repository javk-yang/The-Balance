package com.finance.repository;

import com.finance.entity.LoanPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Long> {
    List<LoanPayment> findByLoanIdAndUserIdOrderByPaymentDateDesc(Long loanId, Long userId);
    List<LoanPayment> findByUserIdAndPaymentDateBetween(Long userId, LocalDate start, LocalDate end);
}
