package com.rev.app.service;

import com.rev.app.entity.Favorite;
import java.util.List;

public interface IFavoriteService {

    Favorite addFavorite(Favorite favorite);

    void removeFavorite(Long id);

    List<Favorite> getFavoritesByUserId(Long userId);

    void toggleFavorite(Long userId, Long productId);
}
