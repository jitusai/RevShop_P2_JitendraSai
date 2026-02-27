package com.rev.app.mapper;

import com.rev.app.dto.OrderDTO;
import com.rev.app.entity.Order;
import com.rev.app.entity.Product;
import com.rev.app.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

//    public static OrderDTO toDTO(Order order) {
//        if (order == null) return null;
//
//        OrderDTO dto = new OrderDTO();
//        dto.setId(order.getId());
//        dto.setUserId(order.getUser() != null ? order.getUser().getId() : null);
//        dto.setProductIds(
//                order.getProducts() != null
//                        ? order.getProducts().stream().map(Product::getId).collect(Collectors.toList())
//                        : null
//        );
//        dto.setTotalAmount(order.getTotalAmount());
//        dto.setStatus(order.getStatus());
//        return dto;
//    }
//
//    public static Order toEntity(OrderDTO dto, User user, List<Product> products) {
//        if (dto == null || user == null || products == null) return null;
//
//        Order order = new Order();
//        order.setUser(user);
//        order.setProducts(products);
//        order.setTotalAmount(dto.getTotalAmount());
//        order.setStatus(dto.getStatus());
//        return order;
//    }
}