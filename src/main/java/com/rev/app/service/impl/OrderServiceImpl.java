package com.rev.app.service.impl;

import com.rev.app.dto.CartItemDTO;
import com.rev.app.dto.OrderDTO;
import com.rev.app.dto.OrderItemDTO;
import com.rev.app.dto.UserDTO;
import com.rev.app.entity.*;
import com.rev.app.entity.enums.OrderStatus;
import com.rev.app.entity.enums.PaymentMethod;
import com.rev.app.entity.enums.PaymentStatus;
import com.rev.app.exception.InsufficientStockException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.mapper.OrderMapper;
import com.rev.app.mapper.UserMapper;
import com.rev.app.repository.OrderRepository;
import com.rev.app.repository.ProductRepository;
import com.rev.app.service.INotificationService;
import com.rev.app.service.IOrderService;
import com.rev.app.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final INotificationService INotificationService;
    private final IUserService IUserService;

    @Override
    @Transactional
    public OrderDTO placeOrder(OrderDTO orderDTO) {
        UserDTO buyerDTO = IUserService.findById(orderDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + orderDTO.getUserId()));

        List<CartItemDTO> cartItems = new ArrayList<>();
        if (orderDTO.getItems() != null) {
            for (OrderItemDTO i : orderDTO.getItems()) {
                CartItemDTO item = new CartItemDTO();
                item.setProductId(i.getProductId());
                item.setQuantity(i.getQuantity());
                cartItems.add(item);
            }
        }

        return placeOrder(buyerDTO, cartItems, orderDTO.getPaymentMethod(), orderDTO.getShippingAddress());
    }

    @Override
    @Transactional
    public OrderDTO placeOrder(UserDTO buyerDTO, Long productId, Integer quantity, String paymentMethodStr,
            String address) {
        CartItemDTO item = new CartItemDTO();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return placeOrder(buyerDTO, List.of(item), paymentMethodStr, address);
    }

    @Override
    @Transactional
    public OrderDTO placeOrder(UserDTO buyerDTO, List<CartItemDTO> items, String paymentMethodStr, String address) {
        User buyer = UserMapper.toEntity(buyerDTO);
        validateStock(items);

        Order order = new Order();
        order.setBuyer(buyer);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(address);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItemDTO cartItem : items) {
            Product p = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + cartItem.getProductId()));

            BigDecimal unitPrice = p.getDiscountedPrice() != null ? p.getDiscountedPrice() : p.getPrice();
            total = total.add(unitPrice.multiply(new BigDecimal(cartItem.getQuantity())));

            orderItems.add(OrderItem.builder()
                    .order(order)
                    .product(p)
                    .quantity(cartItem.getQuantity())
                    .price(unitPrice)
                    .build());

            // Atomically decrement stock
            p.setQuantity(p.getQuantity() - cartItem.getQuantity());
            productRepository.save(p);
        }
        order.setTotalAmount(total);
        order.setItems(orderItems);

        PaymentMethod method = parsePaymentMethod(paymentMethodStr);
        PaymentStatus initialStatus = (method == PaymentMethod.CASH_ON_DELIVERY) ? PaymentStatus.INITIATED
                : PaymentStatus.SUCCESS;

        Payment payment = Payment.builder()
                .order(order)
                .method(method)
                .status(initialStatus)
                .amount(total)
                .build();
        order.setPayment(payment);

        updateBuyerAddress(buyer, address);

        Order saved = orderRepository.save(order);
        sendOrderNotifications(saved);

        return OrderMapper.toDTO(saved);
    }

    private void validateStock(List<CartItemDTO> items) {
        for (CartItemDTO item : items) {
            Product p = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.getProductId()));
            if (p.getQuantity() == null || p.getQuantity() < item.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + p.getName());
            }
        }
    }

    private PaymentMethod parsePaymentMethod(String methodStr) {
        try {
            return PaymentMethod.valueOf(methodStr);
        } catch (Exception e) {
            return PaymentMethod.CASH_ON_DELIVERY;
        }
    }

    private void updateBuyerAddress(User buyer, String address) {
        if (address != null && !address.isBlank()) {
            boolean addressExists = address.equals(buyer.getAddress());
            if (!addressExists && buyer.getAdditionalAddresses() != null) {
                addressExists = buyer.getAdditionalAddresses().contains(address);
            }
            if (!addressExists) {
                if (buyer.getAddress() == null || buyer.getAddress().isBlank()) {
                    buyer.setAddress(address);
                } else {
                    if (buyer.getAdditionalAddresses() == null) {
                        buyer.setAdditionalAddresses(new ArrayList<>());
                    }
                    buyer.getAdditionalAddresses().add(address);
                }
                IUserService.saveUser(UserMapper.toDTO(buyer));
            }
        }
    }

    private void sendOrderNotifications(Order saved) {
        if (saved.getBuyer() != null) {
            Notification buyerNotification = new Notification();
            buyerNotification.setUser(saved.getBuyer());
            buyerNotification.setMessage("🎉 Success! Your order #" + saved.getId() + " has been placed.");
            buyerNotification.setReadStatus(false);
            INotificationService.sendNotification(buyerNotification);
        }

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
                        INotificationService.sendNotification(notification);
                    }
                }
            }
        }
    }

    @Override
    public Optional<OrderDTO> findById(Long id) {
        return orderRepository.findById(id).map(OrderMapper::toDTO);
    }

    @Override
    public List<OrderDTO> findByBuyerId(Long buyerId) {
        return orderRepository.findByBuyerId(buyerId).stream()
                .map(OrderMapper::toDTO)
                .toList();
    }

    @Override
    public List<OrderDTO> findBySellerId(Long sellerId) {
        return orderRepository.findAllBySellerId(sellerId).stream()
                .map(OrderMapper::toDTO)
                .toList();
    }

    @Override
    public List<OrderDTO> findAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public OrderDTO updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(newStatus);

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

        if (newStatus != previousStatus && order.getBuyer() != null) {
            Notification notification = new Notification();
            notification.setUser(order.getBuyer());
            notification.setMessage("📋 Your order #" + order.getId() + " status is now: " + newStatus);
            notification.setReadStatus(false);
            INotificationService.sendNotification(notification);
        }

        return OrderMapper.toDTO(savedOrder);
    }

    @Override
    public boolean hasDeliveredOrderForProduct(Long buyerId, Long productId) {
        return orderRepository.hasDeliveredOrderForProduct(buyerId, productId);
    }
}
