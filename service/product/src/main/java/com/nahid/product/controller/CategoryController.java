package com.nahid.product.controller;

import com.nahid.product.dto.response.CategoryResponseDto;
import com.nahid.product.dto.request.CreateCategoryRequestDto;
import com.nahid.product.dto.response.ApiResponse;
import com.nahid.product.service.CategoryService;
import com.nahid.product.util.constant.ApiResponseConstant;
import com.nahid.product.util.helper.ApiResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(@Valid @RequestBody CreateCategoryRequestDto request) {
        CategoryResponseDto response = categoryService.createCategory(request);
        return ApiResponseUtil.success(response, ApiResponseConstant.CATEGORY_CREATED_SUCCESSFULLY, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryById(@PathVariable Long id) {
        CategoryResponseDto response = categoryService.getCategoryById(id);
        return ApiResponseUtil.success(response, ApiResponseConstant.CATEGORY_FETCHED_SUCCESSFULLY);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getAllCategories() {
        List<CategoryResponseDto> response = categoryService.getAllCategories();
        return ApiResponseUtil.success(response, ApiResponseConstant.CATEGORIES_FETCHED_SUCCESSFULLY);
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getActiveCategories() {
        List<CategoryResponseDto> response = categoryService.getActiveCategories();
        return ApiResponseUtil.success(response, ApiResponseConstant.ACTIVE_CATEGORIES_FETCHED_SUCCESSFULLY);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponseUtil.success(null, ApiResponseConstant.CATEGORY_DELETED_SUCCESSFULLY, HttpStatus.NO_CONTENT);
    }
}
