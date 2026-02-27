package com.rev.app.service;

import com.rev.app.entity.Review;

import java.util.List;

public interface ReviewService {

    Review addReview(Review review);

    List<Review> findByProductId(Long productId);
}