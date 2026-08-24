package com.finance.service;

import com.finance.common.SecurityUtils;
import com.finance.dto.LoanPaymentRequest;
import com.finance.dto.LoanRequest;
import com.finance.entity.Loan;
import com.finance.entity.LoanPayment;
import com.finance.repository.LoanPaymentRepository;
import com.finance.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoanService {
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private LoanPaymentRepository loanPaymentRepository;

    public List<Loan> getAllLoans() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Loan> loans = loanRepository.findByUserIdOrderByCreatedAtDesc(userId);
        loans.forEach(loan -> loan.calculateStats(loanPaymentRepository.findByLoanIdAndUserIdOrderByPaymentDateDesc(loan.getId(), userId)));
        return loans;
    }

    public Loan getLoan(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Loan loan = findOwnedLoan(id, userId);
        loan.calculateStats(loanPaymentRepository.findByLoanIdAndUserIdOrderByPaymentDateDesc(id, userId));
        return loan;
    }

    public Loan createLoan(LoanRequest request) {
        Loan loan = new Loan();
        loan.setUserId(SecurityUtils.getCurrentUserId());
        copyRequest(loan, request, true);
        return loanRepository.save(loan);
    }

    public Loan updateLoan(Long id, LoanRequest request) {
        Loan loan = findOwnedLoan(id, SecurityUtils.getCurrentUserId());
        copyRequest(loan, request, false);
        return loanRepository.save(loan);
    }

    @Transactional
    public void deleteLoan(Long id) {
        Loan loan = findOwnedLoan(id, SecurityUtils.getCurrentUserId());
        loanPaymentRepository.deleteAll(loanPaymentRepository.findByLoanIdAndUserIdOrderByPaymentDateDesc(id, loan.getUserId()));
        loanRepository.delete(loan);
    }

    public List<LoanPayment> getPayments(Long loanId) {
        Long userId = SecurityUtils.getCurrentUserId();
        findOwnedLoan(loanId, userId);
        return loanPaymentRepository.findByLoanIdAndUserIdOrderByPaymentDateDesc(loanId, userId);
    }

    @Transactional
    public LoanPayment addPayment(Long loanId, LoanPaymentRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Loan loan = findOwnedLoan(loanId, userId);
        BigDecimal amount = request.getAmount();
        BigDecimal principalAmount = request.getPrincipalAmount() != null ? request.getPrincipalAmount() : amount;
        BigDecimal interestAmount = request.getInterestAmount() != null ? request.getInterestAmount() : BigDecimal.ZERO;
        if (principalAmount.add(interestAmount).compareTo(amount) > 0) {
            throw new RuntimeException("本金和利息不能超过还款金额");
        }
        List<LoanPayment> existingPayments = loanPaymentRepository.findByLoanIdAndUserIdOrderByPaymentDateDesc(loanId, userId);
        loan.calculateStats(existingPayments);
        if (principalAmount.compareTo(loan.getRemainingPrincipal()) > 0) {
            throw new RuntimeException("归还本金不能超过剩余本金");
        }
        LoanPayment payment = new LoanPayment();
        payment.setUserId(userId);
        payment.setLoanId(loanId);
        payment.setAmount(amount);
        payment.setPaymentDate(request.getPaymentDate());
        payment.setPrincipalAmount(principalAmount);
        payment.setInterestAmount(interestAmount);
        payment.setNote(request.getNote());
        return loanPaymentRepository.save(payment);
    }

    @Transactional
    public void deletePayment(Long loanId, Long paymentId) {
        Long userId = SecurityUtils.getCurrentUserId();
        findOwnedLoan(loanId, userId);
        LoanPayment payment = loanPaymentRepository.findById(paymentId)
                .filter(p -> p.getLoanId().equals(loanId) && p.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("还款记录不存在"));
        loanPaymentRepository.delete(payment);
    }

    public Map<String, BigDecimal> getOverview() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Loan> loans = loanRepository.findByUserIdOrderByCreatedAtDesc(userId);
        BigDecimal totalBorrowed = loans.stream().map(Loan::getPrincipal).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = BigDecimal.ZERO;
        BigDecimal paidInterest = BigDecimal.ZERO;
        BigDecimal currentMonthPaid = BigDecimal.ZERO;
        BigDecimal currentMonthDue = BigDecimal.ZERO;
        for (Loan loan : loans) {
            List<LoanPayment> payments = loanPaymentRepository.findByLoanIdAndUserIdOrderByPaymentDateDesc(loan.getId(), userId);
            loan.calculateStats(payments);
            remaining = remaining.add(loan.getRemainingPrincipal());
            paidInterest = paidInterest.add(loan.getPaidInterest());
            if (!"CLOSED".equalsIgnoreCase(loan.getStatus()) && !"PAID".equalsIgnoreCase(loan.getStatus())) {
                currentMonthDue = currentMonthDue.add(loan.getMonthlyPayment() != null ? loan.getMonthlyPayment() : BigDecimal.ZERO);
            }
            currentMonthPaid = currentMonthPaid.add(payments.stream()
                    .filter(p -> p.getPaymentDate() != null && YearMonth.from(p.getPaymentDate()).equals(YearMonth.now()))
                    .map(LoanPayment::getAmount).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        result.put("totalBorrowed", totalBorrowed);
        result.put("remainingPrincipal", remaining);
        result.put("currentMonthDue", currentMonthDue);
        result.put("currentMonthPaid", currentMonthPaid);
        result.put("paidInterest", paidInterest);
        result.put("loanCount", BigDecimal.valueOf(loans.size()));
        return result;
    }

    private Loan findOwnedLoan(Long id, Long userId) {
        return loanRepository.findById(id).filter(l -> l.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("贷款不存在"));
    }

    private void copyRequest(Loan loan, LoanRequest request, boolean create) {
        loan.setName(request.getName());
        loan.setLender(request.getLender());
        loan.setType(request.getType());
        loan.setPrincipal(request.getPrincipal());
        loan.setAnnualRate(request.getAnnualRate());
        loan.setTermMonths(request.getTermMonths());
        loan.setStartDate(request.getStartDate() != null ? request.getStartDate() : (create ? LocalDate.now() : loan.getStartDate()));
        loan.setPaymentDay(request.getPaymentDay());
        loan.setMonthlyPayment(request.getMonthlyPayment());
        if (request.getStatus() != null && !request.getStatus().isBlank()) loan.setStatus(request.getStatus());
        else if (create) loan.setStatus("ACTIVE");
        loan.setRemark(request.getRemark());
    }
}
