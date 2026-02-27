package com.rev.app.service;

import com.rev.app.entity.Favorite;

public interface FavoriteService {

    Favorite addFavorite(Favorite favorite);

    void removeFavorite(Long id);
}
