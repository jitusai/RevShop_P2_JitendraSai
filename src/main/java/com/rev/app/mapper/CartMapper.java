package com.rev.app.mapper;

import com.rev.app.dto.CartDTO;
import com.rev.app.dto.CartItemDTO;
import com.rev.app.entity.Cart;
import com.rev.app.entity.CartItem;

import java.util.stream.Collectors;
import java.util.ArrayList;

public class CartMapper {

    public static CartDTO toDTO(Cart cart) {
        if (cart == null)
            return null;
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser() != null ? cart.getUser().getId() : null);
        dto.setItems(cart.getItems() != null
                ? cart.getItems().stream().map(CartMapper::toCartItemDTO).collect(Collectors.toList())
                : new ArrayList<>());
        return dto;
    }

    public static CartItemDTO toCartItemDTO(CartItem item) {
        if (item == null)
            return null;
        CartItemDTO dto = new CartItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct() != null ? item.getProduct().getId() : null);
        dto.setProductName(item.getProduct() != null ? item.getProduct().getName() : null);
        dto.setQuantity(item.getQuantity() != null ? item.getQuantity() : 0);
        dto.setProductStock(
                item.getProduct() != null && item.getProduct().getQuantity() != null ? item.getProduct().getQuantity()
                        : 0);
        dto.setTotalPrice(item.getTotalPrice() != null ? item.getTotalPrice().doubleValue() : 0.0);
        dto.setProductImageUrl(item.getProduct() != null ? item.getProduct().getImageUrl() : null);
        return dto;
    }

    public static Cart toEntity(CartDTO dto) {
        if (dto == null)
            return null;
        Cart cart = new Cart();
        cart.setId(dto.getId());
        // User and Items are typically handled by service layer during updates/saves
        return cart;
    }
}