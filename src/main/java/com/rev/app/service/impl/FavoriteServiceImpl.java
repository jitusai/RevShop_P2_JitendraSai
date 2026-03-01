package com.rev.app.service.impl;

import com.rev.app.entity.Favorite;
import com.rev.app.repository.FavoriteRepository;
import com.rev.app.repository.ProductRepository;
import com.rev.app.repository.UserRepository;
import com.rev.app.service.IFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements IFavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public Favorite addFavorite(Favorite favorite) {
        return favoriteRepository.save(favorite);
    }

    @Override
    public void removeFavorite(Long id) {
        favoriteRepository.deleteById(id);
    }

    @Override
    public List<Favorite> getFavoritesByUserId(Long userId) {
        return favoriteRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void toggleFavorite(Long userId, Long productId) {
        Favorite existing = favoriteRepository.findByUserAndProduct(userId, productId);
        if (existing != null) {
            favoriteRepository.delete(existing);
        } else {
            Favorite favorite = new Favorite();
            userRepository.findById(userId).ifPresent(favorite::setUser);
            productRepository.findById(productId).ifPresent(favorite::setProduct);
            favoriteRepository.save(favorite);
        }
    }
}
