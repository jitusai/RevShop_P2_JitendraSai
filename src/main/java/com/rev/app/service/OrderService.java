package com.rev.app.service;

import com.rev.app.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    Order placeOrder(Order order);

    Optional<Order> findById(Long id);

    List<Order> findByBuyerId(Long buyerId);
}