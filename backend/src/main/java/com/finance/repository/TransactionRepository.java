package com.finance.repository;

import com.finance.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * 分页查询用户交易流水，支持按时间范围、分类、类型、关键词筛选
     */
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId " +
           "AND (:startDate IS NULL OR t.date >= :startDate) " +
           "AND (:endDate IS NULL OR t.date <= :endDate) " +
           "AND (:categoryId IS NULL OR t.categoryId = :categoryId) " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:keyword IS NULL OR t.note LIKE CONCAT('%', :keyword, '%')) " +
           "ORDER BY t.date DESC, t.createdAt DESC")
    Page<Transaction> findByFilters(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryId") Long categoryId,
            @Param("type") String type,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 查询某个时间范围内的所有交易（用于统计）
     */
    List<Transaction> findByUserIdAndDateBetweenOrderByDateDesc(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 查询用户最近 N 天的交易
     */
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId ORDER BY t.date DESC, t.createdAt DESC LIMIT :limit")
    List<Transaction> findRecentTransactions(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 统计某个时间段内某类型的总金额
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.userId = :userId " +
           "AND t.type = :type " +
           "AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumByTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("type") String type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 按分类统计某个时间段的支出金额
     */
    @Query("SELECT t.categoryId, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.userId = :userId AND t.type = 'EXPENSE' " +
           "AND t.date BETWEEN :startDate AND :endDate " +
           "GROUP BY t.categoryId")
    List<Object[]> sumExpenseByCategory(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 按日期统计金额（用于趋势图）
     */
    @Query("SELECT t.date, t.type, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.userId = :userId " +
           "AND t.date BETWEEN :startDate AND :endDate " +
           "GROUP BY t.date, t.type ORDER BY t.date")
    List<Object[]> dailySumByType(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 统计某月某分类的支出总额（用于预算进度）
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.userId = :userId AND t.categoryId = :categoryId " +
           "AND t.type = 'EXPENSE' " +
           "AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumExpenseByCategoryAndDateRange(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
