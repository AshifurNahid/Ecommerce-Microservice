package com.nahid.product.service;



import com.nahid.product.dto.CategoryResponseDto;
import com.nahid.product.dto.CreateCategoryRequestDto;

import java.util.List;

public interface CategoryService {

    CategoryResponseDto createCategory(CreateCategoryRequestDto request);

    CategoryResponseDto getCategoryById(Long id);

    List<CategoryResponseDto> getAllCategories();

    List<CategoryResponseDto> getActiveCategories();



    void deleteCategory(Long id);
}