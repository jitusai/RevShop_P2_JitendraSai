package com.rev.app.mapper;

import com.rev.app.dto.ReviewDTO;
import com.rev.app.entity.Review;
import com.rev.app.entity.User;
import com.rev.app.entity.Product;

public class ReviewMapper {

//    public static ReviewDTO toDTO(Review review) {
//        if (review == null) return null;
//
//        ReviewDTO dto = new ReviewDTO();
//        dto.setId(review.getId());
//        dto.setUserId(review.getUser() != null ? review.getUser().getId() : null);
//        dto.setProductId(review.getProduct() != null ? review.getProduct().getId() : null);
//        dto.setRating(review.getRating());
//        dto.setComment(review.getComment());
//        return dto;
//    }
//
//    public static Review toEntity(ReviewDTO dto, User user, Product product) {
//        if (dto == null || user == null || product == null) return null;
//
//        Review review = new Review();
//        review.setUser(user);
//        review.setProduct(product);
//        review.setRating(dto.getRating());
//        review.setComment(dto.getComment());
//        return review;
//    }
}