package com.rev.app.controller;

import com.rev.app.service.ICartService;
import com.rev.app.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final ICartService ICartService;
    private final IUserService IUserService;

    @GetMapping
    public String viewCart(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        IUserService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
            ICartService.getCartByUserId(user.getId()).ifPresent(cart -> {
                model.addAttribute("cart", cart);
                BigDecimal total = cart.getItems().stream()
                        .map(item -> BigDecimal.valueOf(item.getTotalPrice()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                model.addAttribute("cartTotal", total);
            });
        });
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
            @RequestParam("quantity") Integer quantity,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            IUserService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                ICartService.addItemToCart(user.getId(), productId, quantity);
            });
            redirectAttributes.addFlashAttribute("successMsg", "Added to cart! 🛒");
        } catch (com.rev.app.exception.InsufficientStockException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Stock issue: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Could not add to cart: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam("cartItemId") Long cartItemId,
            @RequestParam("quantity") Integer quantity,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            IUserService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                ICartService.updateItemQuantity(user.getId(), cartItemId, quantity);
            });
            redirectAttributes.addFlashAttribute("successMsg", "Cart updated! ✨");
        } catch (com.rev.app.exception.InsufficientStockException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Stock issue: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Could not update cart: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam("cartItemId") Long cartItemId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            IUserService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                ICartService.removeItemFromCart(user.getId(), cartItemId);
            });
            redirectAttributes.addFlashAttribute("successMsg", "Item removed from cart.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Could not remove item: " + e.getMessage());
        }
        return "redirect:/cart";
    }
}
