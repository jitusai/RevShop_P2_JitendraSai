package com.rev.app.service;

import com.rev.app.dto.ProductDTO;
import com.rev.app.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    Product addProduct(Product product);

    Product updateProduct(Product product);

    void updateProduct(Long id, Product product, Long categoryId);

    void deleteProduct(Long id);

    Optional<Product> findById(Long id);

    List<Product> findAll();

    List<ProductDTO> findAllDTOs();

    List<ProductDTO> searchByName(String keyword);

    void saveProduct(Product product);

    void saveProduct(Product product, String sellerEmail, Long categoryId);

    List<ProductDTO> getNewArrivals();

    /**
     * Returns all products that belong to the named category (case-insensitive).
     */
    List<ProductDTO> getProductsByCategoryName(String categoryName);

    List<ProductDTO> findBySellerId(Long sellerId);
}
