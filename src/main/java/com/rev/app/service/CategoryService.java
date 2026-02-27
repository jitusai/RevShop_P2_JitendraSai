package com.rev.app.service;

import com.rev.app.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    Category createCategory(Category category);

    Category updateCategory(Long id, Category category);

    void deleteCategory(Long id);

    Optional<Category> findById(Long id);

    List<Category> findAll();

    Optional<Category> findByName(String name);
}