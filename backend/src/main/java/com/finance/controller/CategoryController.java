package com.finance.controller;

import com.finance.common.Result;
import com.finance.dto.CategoryRequest;
import com.finance.entity.Category;
import com.finance.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 消费分类（消费板块）接口 - 支持自定义新增/编辑/删除
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 查询分类列表（支持按类型筛选：INCOME/EXPENSE）
     */
    @GetMapping
    public Result<List<Category>> list(@RequestParam(required = false) String type) {
        return Result.success(categoryService.getAllCategories(type));
    }

    /**
     * 新增消费分类
     */
    @PostMapping
    public Result<Category> create(@Valid @RequestBody CategoryRequest request) {
        return Result.success(categoryService.createCategory(request));
    }

    /**
     * 编辑分类
     */
    @PutMapping("/{id}")
    public Result<Category> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return Result.success(categoryService.updateCategory(id, request));
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}
