package com.rev.app.controller;

import com.rev.app.entity.Order;
import com.rev.app.entity.User;
import com.rev.app.service.OrderService;
import com.rev.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @GetMapping
    public String viewOrderHistory(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        userService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
            model.addAttribute("orders", orderService.findByBuyerId(user.getId()));
        });
        return "orders";
    }

    @PostMapping("/place")
    public String placeOrder(@AuthenticationPrincipal UserDetails userDetails, Order order) {
        userService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
            order.setBuyer(user);
            orderService.placeOrder(order);
        });
        return "redirect:/orders";
    }
}
