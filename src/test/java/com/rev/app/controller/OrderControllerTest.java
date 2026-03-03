package com.rev.app.controller;

import com.rev.app.config.TestSecurityConfig;
import java.math.BigDecimal;
import com.rev.app.dto.OrderDTO;
import com.rev.app.dto.UserDTO;
import com.rev.app.entity.enums.OrderStatus;
import com.rev.app.repository.OrderItemRepository;
import com.rev.app.security.CustomUserDetailsService;
import com.rev.app.security.JwtAuthenticationFilter;
import com.rev.app.security.JwtUtil;
import com.rev.app.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link OrderController}.
 * Uses @WebMvcTest with mocked services and OrderItemRepository.
 */
@WebMvcTest(controllers = OrderController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IOrderService iOrderService;
    @MockBean
    private IUserService iUserService;
    @MockBean
    private IProductService iProductService;
    @MockBean
    private OrderItemRepository orderItemRepository;
    @MockBean
    private IReviewService iReviewService;
    @MockBean
    private IFavoriteService iFavoriteService;

    private UserDTO buyerDTO;
    private UserDTO sellerDTO;

    @BeforeEach
    void setUp() {
        buyerDTO = new UserDTO();
        buyerDTO.setId(1L);
        buyerDTO.setEmail("buyer@test.com");

        sellerDTO = new UserDTO();
        sellerDTO.setId(2L);
        sellerDTO.setEmail("seller@test.com");
    }

    // ─── GET /orders (buyer) ──────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("GET /orders - buyer sees their order history")
    void viewOrderHistory_buyer_returnsOrdersView() throws Exception {
        OrderDTO order = new OrderDTO();
        order.setId(1L);

        when(iUserService.findByEmail("buyer@test.com")).thenReturn(Optional.of(buyerDTO));
        when(iOrderService.findByBuyerId(1L)).thenReturn(List.of(order));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"));
    }

    // ─── POST /orders/place ───────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("POST /orders/place - places order and redirects to confirmation")
    void placeOrder_success_redirectsToConfirmation() throws Exception {
        when(iUserService.findByEmail("buyer@test.com")).thenReturn(Optional.of(buyerDTO));

        mockMvc.perform(post("/orders/place").with(csrf())
                        .param("productId", "1")
                        .param("quantity", "2")
                        .param("paymentMethod", "CASH_ON_DELIVERY"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/buyer/order-confirmation"));

        verify(iOrderService).placeOrder(eq(buyerDTO), eq(1L), eq(2), eq("CASH_ON_DELIVERY"), isNull());
    }

    // ─── GET /orders/seller ───────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "seller@test.com", roles = "SELLER")
    @DisplayName("GET /orders/seller - seller sees all orders for their products")
    void viewSellerOrders_seller_returnsSellerOrdersView() throws Exception {
        OrderDTO order = new OrderDTO();
        order.setId(10L);

        when(iUserService.findByEmail("seller@test.com")).thenReturn(Optional.of(sellerDTO));
        when(iOrderService.findBySellerId(2L)).thenReturn(List.of(order));

        mockMvc.perform(get("/orders/seller"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller-orders"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attributeExists("statuses"));
    }

    @Test
    @WithMockUser(username = "seller@test.com", roles = "SELLER")
    @DisplayName("GET /orders/seller - statuses model attribute contains all OrderStatus values")
    void viewSellerOrders_statusesContainAllValues() throws Exception {
        when(iUserService.findByEmail("seller@test.com")).thenReturn(Optional.of(sellerDTO));
        when(iOrderService.findBySellerId(2L)).thenReturn(List.of());

        mockMvc.perform(get("/orders/seller"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("statuses", OrderStatus.values()));
    }

    // ─── POST /orders/{id}/status (success) ───────────────────────────────────

    @Test
    @WithMockUser(username = "seller@test.com", roles = "SELLER")
    @DisplayName("POST /orders/{id}/status - success updates status and redirects to /orders/seller")
    void updateOrderStatus_success_redirectsToSellerOrders() throws Exception {
        when(iOrderService.updateStatus(eq(1L), any(OrderStatus.class))).thenReturn(new com.rev.app.dto.OrderDTO());

        mockMvc.perform(post("/orders/1/status").with(csrf())
                .param("status", "SHIPPED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/seller"));

        verify(iOrderService).updateStatus(1L, OrderStatus.SHIPPED);
    }

    // ─── POST /orders/{id}/status (failure) ───────────────────────────────────

    @Test
    @WithMockUser(username = "seller@test.com", roles = "SELLER")
    @DisplayName("POST /orders/{id}/status - failure adds errorMsg flash and redirects")
    void updateOrderStatus_failure_addsErrorFlashAndRedirects() throws Exception {
        doThrow(new RuntimeException("Update failed"))
                .when(iOrderService).updateStatus(anyLong(), any(OrderStatus.class));

        mockMvc.perform(post("/orders/1/status").with(csrf())
                .param("status", "DELIVERED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/seller"))
                .andExpect(flash().attributeExists("errorMsg"));
    }

    // ─── GET /orders/item/{itemId} ────────────────────────────────────────────

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("GET /orders/item/{itemId} - returns order-item-detail view")
    void viewOrderItemDetail_itemExists_returnsDetailView() throws Exception {
        com.rev.app.entity.OrderItem orderItem = new com.rev.app.entity.OrderItem();
        orderItem.setId(5L);
        orderItem.setPrice(BigDecimal.valueOf(100.0));
        orderItem.setQuantity(2);

        com.rev.app.entity.Product product = new com.rev.app.entity.Product();
        product.setId(10L);
        product.setImageUrl("test.jpg");
        orderItem.setProduct(product);

        when(orderItemRepository.findById(5L)).thenReturn(Optional.of(orderItem));

        mockMvc.perform(get("/orders/item/5"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-item-detail"));
    }
}
