package com.finance.repository;

import com.finance.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserIdAndTypeOrderBySortOrderAsc(Long userId, String type);
    List<Category> findByUserIdOrderBySortOrderAsc(Long userId);
    List<Category> findByUserIdAndTypeAndStatusOrderBySortOrderAsc(Long userId, String type, Integer status);
    List<Category> findByUserIdAndStatusOrderBySortOrderAsc(Long userId, Integer status);
    Optional<Category> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndNameAndType(Long userId, String name, String type);
}
