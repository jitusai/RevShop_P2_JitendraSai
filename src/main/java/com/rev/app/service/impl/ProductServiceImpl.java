package com.rev.app.service.impl;

import com.rev.app.dto.ProductDTO;
import com.rev.app.mapper.ProductMapper;
import com.rev.app.repository.CategoryRepository;
import com.rev.app.repository.ProductRepository;
import com.rev.app.repository.UserRepository;
import com.rev.app.service.INotificationService;
import com.rev.app.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final INotificationService INotificationService;

    @Override
    public com.rev.app.entity.Product addProduct(com.rev.app.entity.Product product) {
        com.rev.app.entity.Product saved = productRepository.save(product);
        checkLowStock(saved);
        return saved;
    }

    public void checkLowStock(com.rev.app.entity.Product product) {
        if (product != null && product.getQuantity() != null) {
            // Use 5 as default threshold if not set
            int threshold = (product.getStockThreshold() != null) ? product.getStockThreshold() : 5;

            if (product.getQuantity() <= threshold) {
                com.rev.app.entity.User seller = product.getSeller();
                if (seller != null) {
                    com.rev.app.entity.Notification notification = new com.rev.app.entity.Notification();
                    notification.setUser(seller);
                    notification.setMessage(
                            "⚠️ Low Stock Alert: " + product.getName() + " has only " + product.getQuantity()
                                    + " left.");
                    notification.setReadStatus(false);
                    INotificationService.sendNotification(notification);
                }
            }
        }
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public Optional<com.rev.app.entity.Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public List<com.rev.app.entity.Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public List<ProductDTO> findAllDTOs() {
        return productRepository.findAll().stream()
                .map(ProductMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> searchByName(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword).stream()
                .map(ProductMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void saveProduct(com.rev.app.entity.Product product) {
        com.rev.app.entity.Product saved = productRepository.save(product);
        checkLowStock(saved);
    }

    @Override
    public void saveProduct(com.rev.app.entity.Product product, String sellerEmail, Long categoryId) {
        userRepository.findByEmail(sellerEmail).ifPresent(user -> {
            product.setSeller(user);
        });
        categoryRepository.findById(categoryId).ifPresent(product::setCategory);
        com.rev.app.entity.Product saved = productRepository.save(product);
        checkLowStock(saved);
    }

    @Override
    public com.rev.app.entity.Product updateProduct(com.rev.app.entity.Product product) {
        com.rev.app.entity.Product saved = productRepository.save(product);
        checkLowStock(saved);
        return saved;
    }

    @Override
    public void updateProduct(Long id, com.rev.app.entity.Product product, Long categoryId) {
        categoryRepository.findById(categoryId).ifPresent(product::setCategory);
        com.rev.app.entity.Product saved = productRepository.save(product);
        checkLowStock(saved);
    }

    @Override
    public List<ProductDTO> getNewArrivals() {
        java.time.LocalDateTime yesterday = java.time.LocalDateTime.now().minusHours(24);
        return productRepository.findTop8ByCreatedAtAfterOrderByIdDesc(yesterday).stream()
                .map(ProductMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getProductsByCategoryName(String categoryName) {
        return categoryRepository.findByNameIgnoreCase(categoryName)
                .map(cat -> productRepository.findByCategoryId(cat.getId()).stream()
                        .map(ProductMapper::toDTO)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<ProductDTO> findBySellerId(Long sellerId) {
        return userRepository.findById(sellerId)
                .map(user -> productRepository.findBySeller(user).stream()
                        .map(ProductMapper::toDTO)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }
}
