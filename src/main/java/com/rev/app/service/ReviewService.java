package com.rev.app.service;

import com.rev.app.entity.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewService {

    Review addReview(Review review);

    List<Review> findByProductId(Long productId);

    Optional<Review> findByProductIdAndUserId(Long productId, Long userId);

    /**
     * Upsert: update if the user already reviewed this product, otherwise create.
     */
    Review saveOrUpdateReview(Long productId, Long userId, Integer rating, String comment, String imageUrl);
}
