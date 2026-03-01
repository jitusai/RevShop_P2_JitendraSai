package com.rev.app.repository;

import com.rev.app.entity.Order;
import com.rev.app.entity.User;
import com.rev.app.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.buyer = :buyer")
    List<Order> findByBuyer(User buyer);

    @Query("SELECT o FROM Order o WHERE o.status = :status")
    List<Order> findByStatus(OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.buyer.id = :buyerId ORDER BY o.createdAt DESC")
    List<Order> findByBuyerId(Long buyerId);

    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.items i WHERE o.buyer.id = :buyerId AND i.product.id = :productId AND o.status = 'DELIVERED'")
    boolean hasDeliveredOrderForProduct(Long buyerId, Long productId);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.product.seller.id = :sellerId ORDER BY o.createdAt DESC")
    List<Order> findAllBySellerId(Long sellerId);
}