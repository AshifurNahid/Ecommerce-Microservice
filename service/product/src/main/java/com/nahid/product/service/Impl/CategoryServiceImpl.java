package com.nahid.product.service.Impl;

import com.nahid.product.dto.response.CategoryResponseDto;
import com.nahid.product.dto.request.CreateCategoryRequestDto;
import com.nahid.product.entity.Category;
import com.nahid.product.exception.DuplicateResourceException;
import com.nahid.product.exception.ResourceNotFoundException;
import com.nahid.product.mapper.CategoryMapper;
import com.nahid.product.repository.CategoryRepository;
import com.nahid.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponseDto createCategory(CreateCategoryRequestDto request) {
        log.info("Creating new category with name: {}", request.getName());

        if (categoryRepository.existsByName(request.getName())) {
            log.error("Category with name {} already exists", request.getName());
            throw new DuplicateResourceException("Category with name " + request.getName() + " already exists");
        }
        Category category = categoryMapper.toEntity(request);

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(Long id) {
        log.debug("Fetching category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Category with ID {} not found", id);
                    return new ResourceNotFoundException("Category not found with ID: " + id);
                });

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAllCategories() {
        log.debug("Fetching all categories");

        List<Category> categories = categoryRepository.findAll();
        return categoryMapper.toResponseList(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getActiveCategories() {
        log.debug("Fetching active categories");

        List<Category> categories = categoryRepository.findByIsActiveTrue();
        return categoryMapper.toResponseList(categories);
    }





    @Override
    public void deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);

        if (!categoryRepository.existsById(id)) {
            log.error("Category with ID {} not found", id);
            throw new ResourceNotFoundException("Category not found with ID: " + id);
        }

        categoryRepository.deleteById(id);
        log.info("Category deleted successfully with ID: {}", id);
    }
}