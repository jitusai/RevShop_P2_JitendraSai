package com.rev.app.controller;

import com.rev.app.entity.enums.OrderStatus;
import com.rev.app.service.*;
import com.rev.app.dto.OrderDTO;
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

    private final IOrderService IOrderService;
    private final IUserService IUserService;
    private final IProductService IProductService;
    private final com.rev.app.repository.OrderItemRepository orderItemRepository;
    private final IReviewService IReviewService;
    private final IFavoriteService IFavoriteService;

    // ── BUYER: Order history ─────────────────────────────────────────────────

    @GetMapping
    public String viewOrderHistory(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        IUserService.findByEmail(userDetails.getUsername())
                .ifPresent(user -> model.addAttribute("orders", IOrderService.findByBuyerId(user.getId())));
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
                IUserService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                    model.addAttribute("currentUserId", user.getId());
                    // canReview = buyer has received this product (DELIVERED order)
                    boolean canReview = IOrderService.hasDeliveredOrderForProduct(user.getId(),
                            item.getProduct().getId());
                    model.addAttribute("canReview", canReview);

                    IReviewService.findByProductIdAndUserId(item.getProduct().getId(), user.getId())
                            .ifPresent(r -> model.addAttribute("myReview", r));

                    boolean isFav = IFavoriteService.getFavoritesByUserId(user.getId()).stream()
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
        IUserService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
            IOrderService.placeOrder(user, productId, quantity, methodStr, null);
        });
        return "redirect:/buyer/order-confirmation";
    }

    // ── SELLER: Manage all orders & update status ────────────────────────────

    @GetMapping("/seller")
    public String viewSellerOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        if (userDetails != null) {
            IUserService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                List<OrderDTO> orders = IOrderService.findBySellerId(user.getId());
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
            IOrderService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMsg", "Order status updated to " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to update status: " + e.getMessage());
        }
        return "redirect:/orders/seller";
    }
}
