package com.rev.app.service.impl;

import com.rev.app.entity.Review;
import com.rev.app.entity.User;
import com.rev.app.entity.Product;
import com.rev.app.repository.ReviewRepository;
import com.rev.app.repository.UserRepository;
import com.rev.app.repository.ProductRepository;
import com.rev.app.service.IReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class ReviewServiceImpl implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public Review addReview(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Review> findByProductId(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Optional<Review> findByProductIdAndUserId(Long productId, Long userId) {
        return reviewRepository.findByProductIdAndUserId(productId, userId);
    }

    /**
     * Upsert: if the user has already reviewed this product, update it; otherwise
     * create.
     */
    @Override
    public Review saveOrUpdateReview(Long productId, Long userId, Integer rating, String comment, String imageUrl) {
        Optional<Review> existing = reviewRepository.findByProductIdAndUserId(productId, userId);
        Review review;
        if (existing.isPresent()) {
            review = existing.get();
            review.setRating(rating);
            review.setComment(comment);
            if (imageUrl != null)
                review.setImageUrl(imageUrl);
            // The ID is already present in 'review' as it was loaded from DB
        } else {
            review = new Review();
            User user = userRepository.findById(userId)
                    .orElseThrow(
                            () -> new com.rev.app.exception.ResourceNotFoundException("User not found: " + userId));
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new com.rev.app.exception.ResourceNotFoundException(
                            "Product not found: " + productId));

            review.setUser(user);
            review.setProduct(product);
            review.setRating(rating);
            review.setComment(comment);
            review.setImageUrl(imageUrl);
        }
        return reviewRepository.save(review);
    }
}
