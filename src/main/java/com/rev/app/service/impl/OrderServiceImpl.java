package com.rev.app.service.impl;

import com.rev.app.entity.Order;
import com.rev.app.repository.OrderRepository;
import com.rev.app.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public Order placeOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<Order> findByBuyerId(Long buyerId) {
        return orderRepository.findByBuyerId(buyerId);
    }
}