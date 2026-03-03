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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

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

    @BeforeEach
    void setUp() {
        // MockitoExtension handles this
    }

    @Test
    public void testPlaceOrder_Success() {
        UserDTO buyerDTO = new UserDTO();
        buyerDTO.setId(1L);
        buyerDTO.setEmail("test@rev.com");

        Product product = new Product();
        product.setId(1L);
        product.setQuantity(10);
        product.setPrice(BigDecimal.valueOf(100));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        OrderDTO orderDTO = orderService.placeOrder(buyerDTO, 1L, 2, "CREDIT_CARD", "123 Street");

        assertNotNull(orderDTO);
        verify(productRepository, times(1)).save(any(Product.class)); // 1 for stock update
    }
}
