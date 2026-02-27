package com.rev.app.config;

import com.rev.app.entity.Category;
import com.rev.app.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        ensureCategory("Electronics");
        ensureCategory("Fashion");
    }

    private void ensureCategory(String name) {
        if (categoryRepository.findByName(name).isEmpty()) {
            Category cat = new Category();
            cat.setName(name);
            categoryRepository.save(cat);
        }
    }
}
