package com.rev.app.mapper;

import com.rev.app.dto.CartDTO;
import com.rev.app.entity.Cart;
import com.rev.app.entity.Product;
import com.rev.app.entity.User;

public class CartMapper {

//    public static CartDTO toDTO(Cart cart) {
//        if (cart == null) return null;
//
//        CartDTO dto = new CartDTO();
//        dto.setId(cart.getId());
//        dto.setUserId(cart.getUser() != null ? cart.getUser().getId() : null);
//        dto.setProductId(cart.getProduct() != null ? cart.getProduct().getId() : null);
//        dto.setQuantity(cart.getQuantity());
//        return dto;
//    }
//
//    public static Cart toEntity(CartDTO dto, User user, Product product) {
//        if (dto == null || user == null || product == null) return null;
//
//        Cart cart = new Cart();
//        cart.setUser(user);
//        cart.setProduct(product);
//        cart.setQuantity(dto.getQuantity());
//        return cart;
//    }
}