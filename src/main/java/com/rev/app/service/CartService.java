package com.rev.app.service;

import com.rev.app.dto.CartDTO;

import java.util.Optional;

public interface CartService {

    CartDTO createCart(CartDTO cartDTO);

    Optional<CartDTO> getCartByUserId(Long userId);

    CartDTO saveCart(CartDTO cartDTO);

    void addItemToCart(Long userId, Long productId, Integer quantity);

    void updateItemQuantity(Long userId, Long cartItemId, Integer quantity);

    void removeItemFromCart(Long userId, Long cartItemId);

    void clearCart(Long userId);
}