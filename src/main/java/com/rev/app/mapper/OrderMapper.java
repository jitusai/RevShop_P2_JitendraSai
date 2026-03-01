package com.rev.app.mapper;

import com.rev.app.dto.OrderDTO;
import com.rev.app.dto.OrderItemDTO;
import com.rev.app.entity.Order;
import com.rev.app.entity.OrderItem;
import com.rev.app.entity.Product;
import com.rev.app.entity.User;

import java.time.LocalDateTime; // Added import for LocalDateTime
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderDTO toDTO(Order order) {
        if (order == null)
            return null;

        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getBuyer() != null ? order.getBuyer().getId() : null);
        dto.setItems(order.getItems() != null
                ? order.getItems().stream().map(OrderMapper::toOrderItemDTO).collect(Collectors.toList())
                : new ArrayList<>());
        dto.setTotalAmount(order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0);
        dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        dto.setOrderDate(order.getCreatedAt()); // From BaseEntity
        dto.setShippingAddress(order.getShippingAddress());
        dto.setBillingAddress(order.getBillingAddress());
        if (order.getPayment() != null) {
            dto.setPaymentMethod(order.getPayment().getMethod() != null ? order.getPayment().getMethod().name() : null);
            dto.setPaymentStatus(order.getPayment().getStatus() != null ? order.getPayment().getStatus().name() : null);
        }

        dto.setBuyerName(order.getBuyer() != null ? order.getBuyer().getName() : "N/A");
        if (order.getItems() != null) {
            dto.setProductNames(order.getItems().stream()
                    .filter(i -> i.getProduct() != null)
                    .map(i -> i.getProduct().getName())
                    .collect(Collectors.toList()));
        } else {
            dto.setProductNames(new ArrayList<>());
        }

        return dto;
    }

    public static OrderItemDTO toOrderItemDTO(OrderItem item) {
        if (item == null)
            return null;
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct() != null ? item.getProduct().getId() : null);
        dto.setProductName(item.getProduct() != null ? item.getProduct().getName() : null);
        dto.setProductImageUrl(item.getProduct() != null ? item.getProduct().getImageUrl() : null);
        dto.setQuantity(item.getQuantity() != null ? item.getQuantity() : 0);
        dto.setPrice(item.getPrice() != null ? item.getPrice().doubleValue() : 0.0);
        return dto;
    }

    public static Order toEntity(OrderDTO dto, User user) {
        if (dto == null || user == null)
            return null;

        Order order = new Order();
        order.setId(dto.getId());
        order.setBuyer(user);
        order.setTotalAmount(java.math.BigDecimal.valueOf(dto.getTotalAmount()));
        if (dto.getStatus() != null) {
            order.setStatus(com.rev.app.entity.enums.OrderStatus.valueOf(dto.getStatus()));
        }
        order.setShippingAddress(dto.getShippingAddress());
        order.setBillingAddress(dto.getBillingAddress());
        return order;
    }
}