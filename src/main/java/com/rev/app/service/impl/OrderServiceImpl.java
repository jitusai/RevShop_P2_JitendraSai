package com.rev.app.service.impl;

import com.rev.app.dto.CartItemDTO;
import com.rev.app.entity.*;
import com.rev.app.entity.enums.OrderStatus;
import com.rev.app.entity.enums.PaymentMethod;
import com.rev.app.entity.enums.PaymentStatus;
import com.rev.app.repository.OrderRepository;
import com.rev.app.repository.ProductRepository;
import com.rev.app.service.NotificationService;
import com.rev.app.service.OrderService;
import com.rev.app.service.ProductService;
import com.rev.app.service.UserService;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.exception.InsufficientStockException;
import com.rev.app.dto.OrderDTO;
import com.rev.app.dto.UserDTO;
import com.rev.app.mapper.OrderMapper;
import com.rev.app.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final ProductService productService;
    private final UserService userService;

    @Override
    @Transactional
    public OrderDTO placeOrder(OrderDTO orderDTO) {
        // Validation: Stock check
        if (orderDTO.getItems() != null) {
            for (var itemDTO : orderDTO.getItems()) {
                Product p = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Product not found: " + itemDTO.getProductId()));
                if (p.getQuantity() == null || p.getQuantity() < itemDTO.getQuantity()) {
                    throw new InsufficientStockException("Insufficient stock for product: " + p.getName());
                }
            }
        }

        // Logic handled usually by mapping back to entity or custom assembly
        // Since placeOrder(Order) was the core, let's keep it but internal or adapted
        // For now, let's adapt it to use Mapper
        User buyer = userService.findById(orderDTO.getUserId())
                .map(UserMapper::toEntity)
                .orElse(null);

        Order order = OrderMapper.toEntity(orderDTO, buyer);
        // Assemble items
        if (orderDTO.getItems() != null) {
            List<OrderItem> items = new ArrayList<>();
            for (var itemDTO : orderDTO.getItems()) {
                Product p = productRepository.findById(itemDTO.getProductId()).orElse(null);
                if (p != null) {
                    items.add(OrderItem.builder()
                            .order(order)
                            .product(p)
                            .quantity(itemDTO.getQuantity())
                            .price(java.math.BigDecimal.valueOf(itemDTO.getPrice()))
                            .build());
                }
            }
            order.setItems(items);
        }

        // Ensure status is PENDING by default
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        }
        Order saved = orderRepository.save(order);

        // Notify Buyer
        if (saved.getBuyer() != null) {
            Notification buyerNotification = new Notification();
            buyerNotification.setUser(saved.getBuyer());
            buyerNotification.setMessage("🎉 Success! Your order #" + saved.getId() + " has been placed.");
            buyerNotification.setReadStatus(false);
            notificationService.sendNotification(buyerNotification);
        }

        // Notify each unique seller whose products are in this order
        if (saved.getItems() != null) {
            Set<Long> notifiedSellerIds = new HashSet<>();
            for (OrderItem item : saved.getItems()) {
                Product product = item.getProduct();
                if (product != null && product.getSeller() != null) {
                    Long sellerId = product.getSeller().getId();
                    if (notifiedSellerIds.add(sellerId)) {
                        Notification notification = new Notification();
                        notification.setUser(product.getSeller());
                        notification.setMessage("📦 New order received! Order #" + saved.getId()
                                + " — " + product.getName()
                                + (saved.getBuyer() != null ? " from " + saved.getBuyer().getName() : ""));
                        notification.setReadStatus(false);
                        notificationService.sendNotification(notification);
                    }
                }
            }
        }

        return OrderMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public OrderDTO placeOrder(UserDTO buyerDTO, Long productId, Integer quantity, String paymentMethodStr,
            String address) {
        User buyer = UserMapper.toEntity(buyerDTO);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getQuantity() == null || product.getQuantity() < quantity) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        BigDecimal unitPrice = product.getDiscountedPrice() != null ? product.getDiscountedPrice() : product.getPrice();
        BigDecimal total = unitPrice.multiply(new BigDecimal(quantity));

        Order order = new Order();
        order.setBuyer(buyer);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(total);
        order.setShippingAddress(address);

        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .price(unitPrice)
                .build();
        order.setItems(new ArrayList<>(List.of(item)));

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(paymentMethodStr);
        } catch (Exception e) {
            method = PaymentMethod.CASH_ON_DELIVERY;
        }

        Payment payment = Payment.builder()
                .order(order)
                .method(method)
                .status(method == PaymentMethod.CASH_ON_DELIVERY ? PaymentStatus.INITIATED : PaymentStatus.SUCCESS)
                .amount(total)
                .build();
        order.setPayment(payment);

        if (address != null && !address.isBlank()) {
            buyer.setAddress(address);
            userService.saveUser(UserMapper.toDTO(buyer));
        }

        // Update Stock
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);

        // Logic for placeOrder(order)
        Order saved = orderRepository.save(order);

        // Notifications... simplified or call the other one?
        // Let's just return via DTO
        return OrderMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public OrderDTO placeOrder(UserDTO buyerDTO, List<CartItemDTO> items, String paymentMethodStr, String address) {
        User buyer = UserMapper.toEntity(buyerDTO);

        // Stock validation
        for (CartItemDTO cartItem : items) {
            Product p = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + cartItem.getProductId()));
            if (p.getQuantity() == null || p.getQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + p.getName());
            }
        }

        // Build order
        Order order = new Order();
        order.setBuyer(buyer);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(address);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItemDTO cartItem : items) {
            Product p = productRepository.findById(cartItem.getProductId()).orElse(null);
            if (p == null)
                continue;
            BigDecimal unitPrice = p.getDiscountedPrice() != null ? p.getDiscountedPrice() : p.getPrice();
            total = total.add(unitPrice.multiply(new BigDecimal(cartItem.getQuantity())));
            orderItems.add(OrderItem.builder()
                    .order(order)
                    .product(p)
                    .quantity(cartItem.getQuantity())
                    .price(unitPrice)
                    .build());
        }
        order.setTotalAmount(total);
        order.setItems(orderItems);

        // Build payment
        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(paymentMethodStr);
        } catch (Exception e) {
            method = PaymentMethod.CASH_ON_DELIVERY;
        }
        Payment payment = Payment.builder()
                .order(order)
                .method(method)
                .status(method == PaymentMethod.CASH_ON_DELIVERY ? PaymentStatus.INITIATED : PaymentStatus.SUCCESS)
                .amount(total)
                .build();
        order.setPayment(payment);

        // Update buyer address
        // Update Stock
        for (CartItemDTO cartItem : items) {
            Product p = productRepository.findById(cartItem.getProductId()).orElse(null);
            if (p != null) {
                p.setQuantity(p.getQuantity() - cartItem.getQuantity());
                productRepository.save(p);
            }
        }

        Order saved = orderRepository.save(order);

        // Notify buyer
        if (saved.getBuyer() != null) {
            Notification buyerNotification = new Notification();
            buyerNotification.setUser(saved.getBuyer());
            buyerNotification.setMessage("🎉 Success! Your order #" + saved.getId() + " has been placed.");
            buyerNotification.setReadStatus(false);
            notificationService.sendNotification(buyerNotification);
        }

        // Notify sellers
        if (saved.getItems() != null) {
            Set<Long> notifiedSellerIds = new HashSet<>();
            for (OrderItem item : saved.getItems()) {
                Product product = item.getProduct();
                if (product != null && product.getSeller() != null) {
                    Long sellerId = product.getSeller().getId();
                    if (notifiedSellerIds.add(sellerId)) {
                        Notification notification = new Notification();
                        notification.setUser(product.getSeller());
                        notification.setMessage("📦 New order received! Order #" + saved.getId()
                                + " — " + product.getName()
                                + (saved.getBuyer() != null ? " from " + saved.getBuyer().getName() : ""));
                        notification.setReadStatus(false);
                        notificationService.sendNotification(notification);
                    }
                }
            }
        }

        return OrderMapper.toDTO(saved);
    }

    @Override
    public Optional<OrderDTO> findById(Long id) {
        return orderRepository.findById(id).map(OrderMapper::toDTO);
    }

    @Override
    public List<OrderDTO> findByBuyerId(Long buyerId) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getBuyer() != null && o.getBuyer().getId().equals(buyerId))
                .map(OrderMapper::toDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<OrderDTO> findBySellerId(Long sellerId) {
        return orderRepository.findAllBySellerId(sellerId).stream()
                .map(OrderMapper::toDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<OrderDTO> findAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderMapper::toDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Updates the order status. When status transitions to DELIVERED,
     * decrements each product's inventory quantity by the ordered amount.
     */
    @Override
    @Transactional
    public OrderDTO updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(newStatus);

        // Stock management when status changes to CANCELLED
        if (newStatus == OrderStatus.CANCELLED && previousStatus != OrderStatus.CANCELLED) {
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    Product product = item.getProduct();
                    if (product != null && item.getQuantity() != null) {
                        int updated = (product.getQuantity() != null ? product.getQuantity() : 0)
                                + item.getQuantity();
                        product.setQuantity(updated);
                        productRepository.save(product);
                    }
                }
            }
        }

        Order savedOrder = orderRepository.save(order);

        // Notify buyer about the status change
        if (newStatus != previousStatus && order.getBuyer() != null) {
            Notification notification = new Notification();
            notification.setUser(order.getBuyer());
            notification.setMessage("📋 Your order #" + order.getId() + " status is now: " + newStatus);
            notification.setReadStatus(false);
            notificationService.sendNotification(notification);
        }

        return OrderMapper.toDTO(savedOrder);
    }

    @Override
    public boolean hasDeliveredOrderForProduct(Long buyerId, Long productId) {
        return orderRepository.hasDeliveredOrderForProduct(buyerId, productId);
    }
}
