package com.rev.app.service;

import com.rev.app.entity.Cart;

import java.util.Optional;

public interface CartService {

    Cart createCart(Cart cart);

    Optional<Cart> getCartByUserId(Long userId);

    Cart saveCart(Cart cart);
}