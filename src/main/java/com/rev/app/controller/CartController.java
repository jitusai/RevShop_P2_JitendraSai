package com.rev.app.controller;

import com.rev.app.service.CartService;
import com.rev.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    public String viewCart(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        userService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
            cartService.getCartByUserId(user.getId()).ifPresent(cart -> {
                model.addAttribute("cart", cart);
                BigDecimal total = cart.getItems().stream()
                        .map(item -> item.getTotalPrice())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                model.addAttribute("cartTotal", total);
            });
        });
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
            @RequestParam("quantity") Integer quantity,
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
            cartService.addItemToCart(user.getId(), productId, quantity);
        });
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam("cartItemId") Long cartItemId,
            @RequestParam("quantity") Integer quantity,
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
            cartService.updateItemQuantity(user.getId(), cartItemId, quantity);
        });
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam("cartItemId") Long cartItemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
            cartService.removeItemFromCart(user.getId(), cartItemId);
        });
        return "redirect:/cart";
    }
}
