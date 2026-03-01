package com.rev.app.service;

import com.rev.app.dto.CartItemDTO;
import com.rev.app.dto.OrderDTO;
import com.rev.app.dto.UserDTO;
import com.rev.app.entity.enums.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    OrderDTO placeOrder(OrderDTO orderDTO);

    /**
     * Specialized method for "Buy Now" - places an order for a single product.
     */
    OrderDTO placeOrder(UserDTO buyer, Long productId, Integer quantity, String paymentMethod,
            String address);

    /**
     * Places an order from a cart (multiple items) with proper Payment handling.
     */
    OrderDTO placeOrder(UserDTO buyer, List<CartItemDTO> items, String paymentMethod, String address);

    Optional<OrderDTO> findById(Long id);

    List<OrderDTO> findByBuyerId(Long buyerId);

    /**
     * Updates the order status. When status is DELIVERED, automatically decrements
     * the quantity of each ordered product in inventory.
     */
    OrderDTO updateStatus(Long orderId, OrderStatus newStatus);

    /** Returns every order — used by the seller view to see all orders. */
    List<OrderDTO> findAllOrders();

    /** Returns all orders containing products belonging to this seller. */
    List<OrderDTO> findBySellerId(Long sellerId);

    /** Returns true if the buyer has a DELIVERED order containing this product. */
    boolean hasDeliveredOrderForProduct(Long buyerId, Long productId);
}
