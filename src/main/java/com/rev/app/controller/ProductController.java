package com.rev.app.controller;

import com.rev.app.entity.Product;
import com.rev.app.entity.User;
import com.rev.app.service.CategoryService;
import com.rev.app.service.ProductService;
import com.rev.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final UserService userService;
    private final CategoryService categoryService;

    // Display All Products
    @GetMapping
    public String viewProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "products";
    }

    // Show Add Form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        return "product-form";
    }

    // Save Product
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute Product product, @AuthenticationPrincipal UserDetails userDetails) {
        userService.findByEmail(userDetails.getUsername()).ifPresent(product::setSeller);
        productService.saveProduct(product);
        return "redirect:/products";
    }

    // Show Edit Form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        productService.findById(id).ifPresent(product -> model.addAttribute("product", product));
        model.addAttribute("categories", categoryService.findAll());
        return "product-form";
    }

    // Update Product
    @PostMapping("/update")
    public String updateProduct(@ModelAttribute Product product, @AuthenticationPrincipal UserDetails userDetails) {
        userService.findByEmail(userDetails.getUsername()).ifPresent(product::setSeller);
        productService.updateProduct(product);
        return "redirect:/products";
    }

    // Delete Product
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }

    // View Product Detail
    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        productService.findById(id).ifPresent(product -> model.addAttribute("product", product));
        return "product-detail";
    }
}