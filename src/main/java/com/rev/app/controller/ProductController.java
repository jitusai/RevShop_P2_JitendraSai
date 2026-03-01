package com.rev.app.controller;

import com.rev.app.entity.Product;
import com.rev.app.service.CategoryService;
import com.rev.app.service.FavoriteService;
import com.rev.app.service.OrderService;
import com.rev.app.service.ProductService;
import com.rev.app.service.ReviewService;
import com.rev.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;
    private final FavoriteService favoriteService;
    private final OrderService orderService;

    @Value("${file.upload-dir:uploads/products}")
    private String uploadDir;

    // ── Helpers ────────────────────────────────────────────────────────────
    /**
     * Saves a multipart image to disk and returns the URL path,
     * e.g. "/uploads/products/abc123.jpg"
     */
    private String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty())
            return null;
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + (ext != null ? "." + ext : "");
            Path dest = dir.resolve(filename);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/products/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image", e);
        }
    }

    // ── Display All Products ────────────────────────────────────────────────
    @GetMapping
    public String viewProducts(Model model) {
        model.addAttribute("products", productService.findAllDTOs());
        model.addAttribute("isSellerView", false);
        return "products";
    }

    // ── Display Seller's Products ───────────────────────────────────────────
    @GetMapping("/seller")
    public String viewSellerProducts(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        userService.findByEmail(userDetails.getUsername()).ifPresentOrElse(user -> {
            model.addAttribute("products", productService.findBySellerId(user.getId()));
            model.addAttribute("isSellerView", true);
        }, () -> {
            model.addAttribute("isSellerView", false);
        });
        return "products";
    }

    // ── Show Add Form ───────────────────────────────────────────────────────
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new com.rev.app.dto.ProductDTO());
        model.addAttribute("categories", categoryService.findAll());
        return "product-form";
    }

    // ── Save NEW Product ────────────────────────────────────────────────────
    @PostMapping("/add")
    public String saveProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam(value = "discountedPrice", required = false) BigDecimal discountedPrice,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("quantity") Integer quantity,
            @RequestParam(value = "stockThreshold", required = false) Integer stockThreshold,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setDiscountedPrice(discountedPrice);
            product.setQuantity(quantity);
            product.setStockThreshold(stockThreshold != null ? stockThreshold : 5);

            if (imageFile != null && !imageFile.isEmpty()) {
                product.setImageUrl(saveImage(imageFile));
            }

            productService.saveProduct(product, userDetails.getUsername(), categoryId);
            redirectAttributes.addFlashAttribute("successMsg", "Product added successfully! 🎉");
            return "redirect:/products/seller";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to add product: " + e.getMessage());
            return "redirect:/products/add";
        }
    }

    // ── Show Edit Form ──────────────────────────────────────────────────────
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        productService.findById(id)
                .ifPresent(product -> model.addAttribute("product", com.rev.app.mapper.ProductMapper.toDTO(product)));
        model.addAttribute("categories", categoryService.findAll());
        return "product-form";
    }

    // ── Update EXISTING Product ─────────────────────────────────────────────
    @PostMapping(value = "/update", consumes = "multipart/form-data")
    public String updateProduct(
            @RequestParam("id") Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam("price") BigDecimal price,
            @RequestParam(value = "discountedPrice", required = false) BigDecimal discountedPrice,
            @RequestParam("quantity") Integer quantity,
            @RequestParam(value = "stockThreshold", required = false) Integer stockThreshold,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @AuthenticationPrincipal UserDetails userDetails) {

        productService.findById(id).ifPresent(existing -> {
            existing.setName(name);
            existing.setDescription(description);
            existing.setPrice(price);
            existing.setDiscountedPrice(discountedPrice);
            existing.setQuantity(quantity);
            existing.setStockThreshold(stockThreshold);

            if (categoryId != null) {
                categoryService.findById(categoryId).ifPresent(existing::setCategory);
            } else {
                existing.setCategory(null);
            }

            // Only replace image if a new one was uploaded
            String newImageUrl = saveImage(imageFile);
            if (newImageUrl != null) {
                existing.setImageUrl(newImageUrl);
            }
            productService.saveProduct(existing);
        });

        return "redirect:/products";
    }

    // ── Delete Product ──────────────────────────────────────────────────────
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMsg", "Product deleted successfully.");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "This product cannot be deleted because it is part of an existing order or is referenced elsewhere.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "An unexpected error occurred while deleting the product.");
        }
        return "redirect:/products";
    }

    // ── View Product Detail ─────────────────────────────────────────────────
    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        productService.findById(id).ifPresent(product -> {
            com.rev.app.dto.ProductDTO dto = com.rev.app.mapper.ProductMapper.toDTO(product);
            model.addAttribute("product", dto);
            model.addAttribute("reviews", dto.getReviews());
        });
        if (userDetails != null) {
            userService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                model.addAttribute("currentUserId", user.getId());
                // Check if product is already in user's favorites
                boolean isFav = favoriteService.getFavoritesByUserId(user.getId()).stream()
                        .anyMatch(f -> f.getProduct().getId().equals(id));
                model.addAttribute("isFav", isFav);
                // canReview = buyer has received this product (DELIVERED order)
                boolean canReview = orderService.hasDeliveredOrderForProduct(user.getId(), id);
                model.addAttribute("canReview", canReview);
                // Pass user's existing review if any
                reviewService.findByProductIdAndUserId(id, user.getId())
                        .ifPresent(r -> model.addAttribute("myReview", r));
            });
        }
        return "product-detail";
    }

    // ── Submit / Update a Review ────────────────────────────────────────────
    @PostMapping(value = "/{id}/review", consumes = "multipart/form-data")
    public String submitReview(@PathVariable Long id,
            @RequestParam("rating") Integer rating,
            @RequestParam(value = "comment", required = false) String comment,
            @RequestParam(value = "reviewImage", required = false) MultipartFile reviewImage,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (userDetails != null) {
            try {
                String imageUrl = (reviewImage != null && !reviewImage.isEmpty()) ? saveImage(reviewImage) : null;
                userService.findByEmail(userDetails.getUsername())
                        .ifPresent(
                                user -> reviewService.saveOrUpdateReview(id, user.getId(), rating, comment, imageUrl));
                redirectAttributes.addFlashAttribute("successMsg", "Review submitted! Thank you. ❤️");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMsg", "Could not save review: " + e.getMessage());
            }
        }
        return "redirect:/products/" + id;
    }
}
