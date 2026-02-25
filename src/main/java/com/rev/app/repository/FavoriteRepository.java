package com.rev.app.repository;

import com.rev.app.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    @Query("SELECT f FROM Favorite f WHERE f.user.id = :userId")
    List<Favorite> findByUserId(Long userId);

    @Query("SELECT f FROM Favorite f WHERE f.user.id = :userId AND f.product.id = :productId")
    Favorite findByUserAndProduct(Long userId, Long productId);
}