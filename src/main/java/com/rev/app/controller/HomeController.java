package com.rev.app.controller;

import com.rev.app.service.INotificationService;
import com.rev.app.service.IProductService;
import com.rev.app.service.IUserService;
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

    private final IProductService IProductService;
    private final IUserService IUserService;
    private final INotificationService INotificationService;

    // ── HOME PAGE ─────────────────────────────────────────────────────────────

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("newArrivals", IProductService.getNewArrivals());
        model.addAttribute("electronics", IProductService.getProductsByCategoryName("Electronics"));
        model.addAttribute("fashion", IProductService.getProductsByCategoryName("Fashion"));
        model.addAttribute("homeKitchen", IProductService.getProductsByCategoryName("Home & Kitchen"));
        model.addAttribute("beauty", IProductService.getProductsByCategoryName("Beauty & Personal Care"));
        model.addAttribute("sports", IProductService.getProductsByCategoryName("Sports & Fitness"));
        model.addAttribute("books", IProductService.getProductsByCategoryName("Books"));
        model.addAttribute("toys", IProductService.getProductsByCategoryName("Toys & Games"));
        return "home";
    }

    // ── CATEGORY BROWSE ───────────────────────────────────────────────────────

    @GetMapping("/category/{name}")
    public String browseCategory(@PathVariable String name, Model model) {
        model.addAttribute("categoryName", name);
        model.addAttribute("products", IProductService.getProductsByCategoryName(name));
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
        IUserService.findByEmail(userDetails.getUsername()).ifPresent(user -> model.addAttribute("notifications",
                INotificationService.getUserNotifications(user.getId())));
        return "notifications";
    }

    @PostMapping({ "/notifications/{id}/acknowledge", "/buyer/notifications/mark-read",
            "/buyer/notifications/{id}/acknowledge" })
    public String acknowledgeNotification(@PathVariable(required = false) Long id) {
        if (id != null) {
            INotificationService.markAsRead(id);
        }
        return "redirect:/notifications";
    }

    // ── SEARCH ────────────────────────────────────────────────────────────────

    @GetMapping("/search")
    public String search(@RequestParam("keyword") String keyword, Model model) {
        model.addAttribute("keyword", keyword);
        model.addAttribute("products", IProductService.searchByName(keyword));
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
