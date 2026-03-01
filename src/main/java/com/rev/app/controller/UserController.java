package com.rev.app.controller;

import com.rev.app.dto.UserDTO;
import com.rev.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserDTO user) {
        if ("ROLE_SELLER".equals(user.getRole())) {
            userService.registerSeller(user);
        } else {
            userService.registerBuyer(user);
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    // ── PROFILE & ADDRESS MANAGEMENT ──────────────────────────────────────────

    @GetMapping("/profile")
    public String viewProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        if (userDetails != null) {
            userService.findByEmail(userDetails.getUsername()).ifPresent(user -> model.addAttribute("user", user));
        }
        return "profile";
    }

    @PostMapping("/profile/address/update")
    public String updateAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("address") String address) {
        if (userDetails != null) {
            userService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                userService.updateAddress(user.getId(), address);
            });
        }
        return "redirect:/profile?success_edit";
    }

    @PostMapping("/profile/address/delete")
    public String deleteAddress(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            userService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                userService.updateAddress(user.getId(), null);
            });
        }
        return "redirect:/profile?success_delete";
    }
}
