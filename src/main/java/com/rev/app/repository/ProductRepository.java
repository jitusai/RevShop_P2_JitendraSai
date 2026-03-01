package com.rev.app.repository;

import com.rev.app.entity.Product;
import com.rev.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    List<Product> findByCategoryId(Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.seller = :seller")
    List<Product> findBySeller(User seller);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchProducts(String keyword);

    @Query("SELECT p FROM Product p WHERE p.quantity <= p.stockThreshold")
    List<Product> findLowStockProducts();

    List<Product> findByNameContainingIgnoreCase(String keyword);

    // Latest 8 products for "New Arrivals" section
    List<Product> findTop8ByOrderByIdDesc();

    // Latest 8 products added within the last 24 hours
    List<Product> findTop8ByCreatedAtAfterOrderByIdDesc(java.time.LocalDateTime date);
}
