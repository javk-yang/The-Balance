package com.finance.service;

import com.finance.common.SecurityUtils;
import com.finance.dto.CategoryRequest;
import com.finance.entity.Category;
import com.finance.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 分类服务 - 消费板块管理（新增/编辑/删除分类）
 */
@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * 查询当前用户所有分类
     */
    public List<Category> getAllCategories(String type) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (type != null && !type.isEmpty()) {
            return categoryRepository.findByUserIdAndTypeAndStatusOrderBySortOrderAsc(userId, type, 1);
        }
        return categoryRepository.findByUserIdAndStatusOrderBySortOrderAsc(userId, 1);
    }

    /**
     * 新增消费分类
     */
    public Category createCategory(CategoryRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 检查是否重复
        if (categoryRepository.existsByUserIdAndNameAndType(userId, request.getName(), request.getType())) {
            throw new RuntimeException("该分类名称已存在");
        }

        Category category = new Category();
        category.setUserId(userId);
        category.setName(request.getName());
        category.setType(request.getType());
        category.setIcon(request.getIcon() != null ? request.getIcon() : "Category");
        category.setColor(request.getColor() != null ? request.getColor() : "#6366f1");
        category.setParentId(request.getParentId());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setStatus(1);

        return categoryRepository.save(category);
    }

    /**
     * 编辑分类
     */
    public Category updateCategory(Long id, CategoryRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Category category = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("分类不存在"));

        category.setName(request.getName());
        category.setType(request.getType());
        if (request.getIcon() != null) category.setIcon(request.getIcon());
        if (request.getColor() != null) category.setColor(request.getColor());
        if (request.getParentId() != null) category.setParentId(request.getParentId());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());

        return categoryRepository.save(category);
    }

    /**
     * 删除分类（软删除）
     */
    @Transactional
    public void deleteCategory(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Category category = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        category.setStatus(0); // 软删除
        categoryRepository.save(category);
    }

    /**
     * 初始化默认分类（新用户注册后调用）
     */
    public void initDefaultCategories(Long userId) {
        String[][] defaults = {
            {"餐饮", "EXPENSE", "🍜", "#ef4444"},
            {"购物", "EXPENSE", "🛍️", "#f59e0b"},
            {"交通", "EXPENSE", "🚗", "#3b82f6"},
            {"住房", "EXPENSE", "🏠", "#10b981"},
            {"娱乐", "EXPENSE", "🎮", "#8b5cf6"},
            {"医疗", "EXPENSE", "💊", "#ec4899"},
            {"教育", "EXPENSE", "📚", "#6366f1"},
            {"其他支出", "EXPENSE", "💸", "#6b7280"},
            {"工资", "INCOME", "💰", "#10b981"},
            {"兼职", "INCOME", "💼", "#3b82f6"},
            {"理财", "INCOME", "📈", "#8b5cf6"},
            {"其他收入", "INCOME", "🎁", "#6b7280"},
        };

        for (int i = 0; i < defaults.length; i++) {
            Category c = new Category();
            c.setUserId(userId);
            c.setName(defaults[i][0]);
            c.setType(defaults[i][1]);
            c.setIcon(defaults[i][2]);
            c.setColor(defaults[i][3]);
            c.setSortOrder(i);
            c.setStatus(1);
            categoryRepository.save(c);
        }
    }
}
