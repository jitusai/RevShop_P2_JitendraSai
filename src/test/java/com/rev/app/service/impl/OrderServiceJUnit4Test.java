package com.rev.app.service.impl;

import com.rev.app.dto.CartItemDTO;
import com.rev.app.dto.OrderDTO;
import com.rev.app.dto.UserDTO;
import com.rev.app.entity.Order;
import com.rev.app.entity.Product;
import com.rev.app.entity.User;
import com.rev.app.repository.OrderRepository;
import com.rev.app.repository.ProductRepository;
import com.rev.app.service.INotificationService;
import com.rev.app.service.IUserService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OrderServiceJUnit4Test {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private INotificationService notificationService;

    @Mock
    private IUserService userService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPlaceOrder_SingleProduct_Success() {
        UserDTO buyerDTO = new UserDTO();
        buyerDTO.setId(1L);
        buyerDTO.setName("Test Buyer");

        Product product = new Product();
        product.setId(10L);
        product.setName("Test Product");
        product.setQuantity(5);
        product.setPrice(BigDecimal.valueOf(100));

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDTO result = orderService.placeOrder(buyerDTO, 10L, 2, "CASH_ON_DELIVERY", "Test Address");

        assertNotNull(result);
        assertEquals(3, product.getQuantity().intValue()); // 5 - 2 = 3
        verify(productRepository).save(product);
        verify(orderRepository).save(any(Order.class));
        verify(notificationService).sendNotification(any());
    }

    @Test
    public void testPlaceOrder_MultipleItems_Success() {
        UserDTO buyerDTO = new UserDTO();
        buyerDTO.setId(1L);

        CartItemDTO item1 = new CartItemDTO();
        item1.setProductId(10L);
        item1.setQuantity(1);

        Product p1 = new Product();
        p1.setId(10L);
        p1.setQuantity(5);
        p1.setPrice(BigDecimal.valueOf(50));

        when(productRepository.findById(10L)).thenReturn(Optional.of(p1));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDTO result = orderService.placeOrder(buyerDTO, Collections.singletonList(item1), "CASH_ON_DELIVERY",
                "Address");

        assertNotNull(result);
        assertEquals(4, p1.getQuantity().intValue());
        verify(productRepository).save(p1);
    }
}
