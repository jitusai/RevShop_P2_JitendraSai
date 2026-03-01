package com.rev.app.controller;

import com.rev.app.service.NotificationService;
import com.rev.app.service.ProductService;
import com.rev.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final UserService userService;
    private final NotificationService notificationService;

    // ── HOME PAGE ─────────────────────────────────────────────────────────────

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("newArrivals", productService.getNewArrivals());
        model.addAttribute("electronics", productService.getProductsByCategoryName("Electronics"));
        model.addAttribute("fashion", productService.getProductsByCategoryName("Fashion"));
        model.addAttribute("homeKitchen", productService.getProductsByCategoryName("Home & Kitchen"));
        model.addAttribute("beauty", productService.getProductsByCategoryName("Beauty & Personal Care"));
        model.addAttribute("sports", productService.getProductsByCategoryName("Sports & Fitness"));
        model.addAttribute("books", productService.getProductsByCategoryName("Books"));
        model.addAttribute("toys", productService.getProductsByCategoryName("Toys & Games"));
        return "home";
    }

    // ── CATEGORY BROWSE ───────────────────────────────────────────────────────

    @GetMapping("/category/{name}")
    public String browseCategory(@PathVariable String name, Model model) {
        model.addAttribute("categoryName", name);
        model.addAttribute("products", productService.getProductsByCategoryName(name));
        return "category";
    }

    // ── BUYER: ORDER CONFIRMATION ──────────────────────────────────────────────

    @GetMapping("/buyer/order-confirmation")
    public String orderConfirmation() {
        return "order-confirmation";
    }

    // ── NOTIFICATIONS ─────────────────────────────────────────────────────────

    @GetMapping("/notifications")
    public String viewNotifications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        userService.findByEmail(userDetails.getUsername()).ifPresent(user -> model.addAttribute("notifications",
                notificationService.getUserNotifications(user.getId())));
        return "notifications";
    }

    @PostMapping({ "/notifications/{id}/acknowledge", "/buyer/notifications/mark-read",
            "/buyer/notifications/{id}/acknowledge" })
    public String acknowledgeNotification(@PathVariable(required = false) Long id) {
        if (id != null) {
            notificationService.markAsRead(id);
        }
        return "redirect:/notifications";
    }

    // ── SEARCH ────────────────────────────────────────────────────────────────

    @GetMapping("/search")
    public String search(@RequestParam("keyword") String keyword, Model model) {
        model.addAttribute("keyword", keyword);
        model.addAttribute("products", productService.searchByName(keyword));
        return "category"; // Reusing category.html for results listing
    }

    // ── GET STARTED / LOGOUT FLOW ─────────────────────────────────────────────

    @GetMapping("/get-started")
    public String getStarted(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return "redirect:/logout";
        }
        return "redirect:/register";
    }
}
