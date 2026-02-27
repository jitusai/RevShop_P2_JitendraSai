package com.rev.app.service.impl;

import com.rev.app.entity.Favorite;
import com.rev.app.repository.FavoriteRepository;
import com.rev.app.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;

    @Override
    public Favorite addFavorite(Favorite favorite) {
        return favoriteRepository.save(favorite);
    }

    @Override
    public void removeFavorite(Long id) {
        favoriteRepository.deleteById(id);
    }
}
