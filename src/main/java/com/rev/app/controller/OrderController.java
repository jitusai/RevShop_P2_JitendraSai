package com.rev.app.controller;

import com.rev.app.entity.enums.OrderStatus;
import com.rev.app.service.OrderService;
import com.rev.app.service.ProductService;
import com.rev.app.service.UserService;
import com.rev.app.dto.OrderDTO;
import com.rev.app.dto.UserDTO;
import com.rev.app.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final ProductService productService;
    private final com.rev.app.repository.OrderItemRepository orderItemRepository;
    private final com.rev.app.service.ReviewService reviewService;
    private final com.rev.app.service.FavoriteService favoriteService;

    // ── BUYER: Order history ─────────────────────────────────────────────────

    @GetMapping
    public String viewOrderHistory(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        userService.findByEmail(userDetails.getUsername())
                .ifPresent(user -> model.addAttribute("orders", orderService.findByBuyerId(user.getId())));
        return "orders";
    }

    @GetMapping("/item/{itemId}")
    public String viewOrderItemDetail(@PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        orderItemRepository.findById(itemId).ifPresent(item -> {
            model.addAttribute("orderItem", item);
            model.addAttribute("product", item.getProduct());
            model.addAttribute("order", item.getOrder());

            if (userDetails != null && item.getProduct() != null) {
                userService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                    model.addAttribute("currentUserId", user.getId());
                    // canReview = buyer has received this product (DELIVERED order)
                    boolean canReview = orderService.hasDeliveredOrderForProduct(user.getId(),
                            item.getProduct().getId());
                    model.addAttribute("canReview", canReview);

                    reviewService.findByProductIdAndUserId(item.getProduct().getId(), user.getId())
                            .ifPresent(r -> model.addAttribute("myReview", r));

                    boolean isFav = favoriteService.getFavoritesByUserId(user.getId()).stream()
                            .anyMatch(f -> f.getProduct().getId().equals(item.getProduct().getId()));
                    model.addAttribute("isFav", isFav);
                });
            }
        });
        return "order-item-detail";
    }

    @PostMapping("/place")
    public String placeOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("productId") Long productId,
            @RequestParam("quantity") Integer quantity,
            @RequestParam(value = "paymentMethod", defaultValue = "CASH_ON_DELIVERY") String methodStr) {
        userService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
            orderService.placeOrder(user, productId, quantity, methodStr, null);
        });
        return "redirect:/buyer/order-confirmation";
    }

    // ── SELLER: Manage all orders & update status ────────────────────────────

    @GetMapping("/seller")
    public String viewSellerOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        if (userDetails != null) {
            userService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                List<OrderDTO> orders = orderService.findBySellerId(user.getId());
                model.addAttribute("orders", orders);
            });
        }
        model.addAttribute("statuses", OrderStatus.values());
        return "seller-orders";
    }

    @PostMapping("/{id}/status")
    public String updateOrderStatus(
            @PathVariable Long id,
            @RequestParam("status") OrderStatus status,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMsg", "Order status updated to " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to update status: " + e.getMessage());
        }
        return "redirect:/orders/seller";
    }
}
