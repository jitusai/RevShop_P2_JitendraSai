package com.rev.app.mapper;

import com.rev.app.dto.ProductDTO;
import com.rev.app.entity.Product;
import java.util.stream.Collectors;

public class ProductMapper {

    public static ProductDTO toDTO(Product product) {
        if (product == null)
            return null;

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setDiscountedPrice(product.getDiscountedPrice());
        dto.setQuantity(product.getQuantity());
        dto.setStockThreshold(product.getStockThreshold());
        dto.setImageUrl(product.getImageUrl());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        if (product.getSeller() != null) {
            dto.setSellerId(product.getSeller().getId());
            dto.setSellerName(product.getSeller().getName());
        }
        if (product.getReviews() != null) {
            dto.setReviews(product.getReviews().stream()
                    .map(ReviewMapper::toDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public static Product toEntity(ProductDTO dto) {
        if (dto == null)
            return null;

        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setDiscountedPrice(dto.getDiscountedPrice());
        product.setQuantity(dto.getQuantity());
        product.setStockThreshold(dto.getStockThreshold());
        product.setImageUrl(dto.getImageUrl());
        // Category and Seller usually set in service layer
        return product;
    }
}