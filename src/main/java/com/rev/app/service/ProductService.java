package com.rev.app.service;

import com.rev.app.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    Product addProduct(Product product);

    Product updateProduct(Product product);

    void deleteProduct(Long id);

    Optional<Product> findById(Long id);

    List<Product> findAll();

    List<Product> searchByName(String keyword);

    void saveProduct(Product product);

    Object getAllProducts();
}