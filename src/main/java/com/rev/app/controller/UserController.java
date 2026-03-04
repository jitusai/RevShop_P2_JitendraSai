package com.rev.app.controller;

import com.rev.app.dto.UserDTO;
import com.rev.app.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final IUserService IUserService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserDTO user, RedirectAttributes redirectAttributes, Model model) {
        // Email check
        if (IUserService.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("errorMsg", "U already have the account! please login");
            model.addAttribute("user", user);
            return "register";
        }

        // Name check
        if (IUserService.existsByName(user.getName())) {
            model.addAttribute("errorMsg", "Please choose another name,This name is already taken");
            model.addAttribute("user", user);
            return "register";
        }

        if ("ROLE_SELLER".equals(user.getRole())) {
            IUserService.registerSeller(user);
        } else {
            IUserService.registerBuyer(user);
        }
        redirectAttributes.addFlashAttribute("successMsg", "User created successfully! Please login.");
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
            IUserService.findByEmail(userDetails.getUsername()).ifPresent(user -> model.addAttribute("user", user));
        }
        return "profile";
    }

    @PostMapping("/profile/address/update")
    public String updateAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("address") String address) {
        if (userDetails != null) {
            IUserService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                IUserService.updateAddress(user.getId(), address);
            });
        }
        return "redirect:/profile?success_edit";
    }

    @PostMapping("/profile/address/delete")
    public String deleteAddress(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            IUserService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                IUserService.updateAddress(user.getId(), null);
            });
        }
        return "redirect:/profile?success_delete";
    }
}
