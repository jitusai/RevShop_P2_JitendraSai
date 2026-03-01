package com.rev.app.controller;

import com.rev.app.service.IFavoriteService;
import com.rev.app.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final IFavoriteService IFavoriteService;
    private final IUserService IUserService;

    @GetMapping
    public String viewFavorites(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        IUserService.findByEmail(userDetails.getUsername())
                .ifPresent(user -> model.addAttribute("favorites", IFavoriteService.getFavoritesByUserId(user.getId())));
        return "favorites";
    }

    @PostMapping("/add")
    public String addToFavorites(@RequestParam("productId") Long productId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        IUserService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
            boolean alreadyFav = IFavoriteService.getFavoritesByUserId(user.getId()).stream()
                    .anyMatch(f -> f.getProduct().getId().equals(productId));
            if (!alreadyFav) {
                IFavoriteService.toggleFavorite(user.getId(), productId);
                redirectAttributes.addFlashAttribute("favMsg", "Added to Favourites ❤️");
            } else {
                redirectAttributes.addFlashAttribute("favMsg", "Already in your Favourites!");
            }
        });
        return "redirect:/products/" + productId;
    }

    @PostMapping("/toggle")
    public String toggleFavorite(@RequestParam("productId") Long productId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        IUserService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
            boolean wasFav = IFavoriteService.getFavoritesByUserId(user.getId()).stream()
                    .anyMatch(f -> f.getProduct().getId().equals(productId));
            IFavoriteService.toggleFavorite(user.getId(), productId);
            if (wasFav) {
                redirectAttributes.addFlashAttribute("favMsg", "Removed from Favourites 🤍");
            } else {
                redirectAttributes.addFlashAttribute("favMsg", "Added to Favourites ❤️");
            }
        });
        return "redirect:/products/" + productId;
    }

    @PostMapping("/remove")
    public String removeFromFavorites(@RequestParam("favoriteId") Long favoriteId,
            RedirectAttributes redirectAttributes) {
        IFavoriteService.removeFavorite(favoriteId);
        redirectAttributes.addFlashAttribute("favMsg", "Removed from Favourites 🤍");
        return "redirect:/favorites";
    }
}
