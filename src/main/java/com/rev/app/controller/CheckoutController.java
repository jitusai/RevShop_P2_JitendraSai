package com.rev.app.controller;

import com.rev.app.service.ICartService;
import com.rev.app.service.IOrderService;
import com.rev.app.service.IProductService;
import com.rev.app.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final ICartService ICartService;
    private final IOrderService IOrderService;
    private final IUserService IUserService;
    private final IProductService IProductService;

    @GetMapping
    public String showCheckout(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "quantity", required = false, defaultValue = "1") Integer quantity,
            Model model) {
        Optional<com.rev.app.dto.UserDTO> userOpt = IUserService.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty())
            return "redirect:/login";

        com.rev.app.dto.UserDTO userDTO = userOpt.get();
        com.rev.app.entity.User user = com.rev.app.mapper.UserMapper.toEntity(userDTO);

        if (productId != null) {
            // ── BUY NOW FLOW ──────────────────────────────────────────────────
            Optional<com.rev.app.entity.Product> productOpt = IProductService.findById(productId);
            if (productOpt.isPresent()) {
                com.rev.app.entity.Product p = productOpt.get();
                BigDecimal itemPrice = p.getDiscountedPrice() != null ? p.getDiscountedPrice() : p.getPrice();
                BigDecimal total = itemPrice.multiply(new BigDecimal(quantity));

                model.addAttribute("isBuyNow", true);
                model.addAttribute("buyNowProduct", p);
                model.addAttribute("buyNowQuantity", quantity);
                model.addAttribute("cartTotal", total);

                // Create a mock cart DTO for the view to avoid null errors and template
                // mismatches
                com.rev.app.dto.CartDTO mockCart = new com.rev.app.dto.CartDTO();
                com.rev.app.dto.CartItemDTO mockItem = new com.rev.app.dto.CartItemDTO();
                mockItem.setProductId(p.getId());
                mockItem.setProductName(p.getName());
                mockItem.setQuantity(quantity);
                mockItem.setProductStock(p.getQuantity() != null ? p.getQuantity() : 0);
                mockItem.setTotalPrice(total.doubleValue());
                mockCart.setItems(List.of(mockItem));
                model.addAttribute("cart", mockCart);

            } else {
                return "redirect:/";
            }
        } else {
            // ── NORMAL CART FLOW ──────────────────────────────────────────────
            Optional<com.rev.app.dto.CartDTO> cartOpt = ICartService.getCartByUserId(user.getId());
            if (cartOpt.isEmpty() || cartOpt.get().getItems().isEmpty()) {
                model.addAttribute("error", "Your cart is empty!");
                model.addAttribute("cart", null);
            } else {
                com.rev.app.dto.CartDTO cart = cartOpt.get();
                BigDecimal total = cart.getItems().stream()
                        .map(item -> BigDecimal.valueOf(item.getTotalPrice()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                model.addAttribute("cart", cart);
                model.addAttribute("cartTotal", total);
            }
            model.addAttribute("isBuyNow", false);
        }

        model.addAttribute("user", user);
        return "checkout";
    }

    @PostMapping("/place")
    public String placeOrder(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("paymentMethod") String methodStr,
            @RequestParam("address") String deliveryAddress,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            RedirectAttributes redirectAttributes) {

        Optional<com.rev.app.dto.UserDTO> userOpt = IUserService.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty())
            return "redirect:/login";

        com.rev.app.dto.UserDTO userDTO = userOpt.get();
        com.rev.app.entity.User user = com.rev.app.mapper.UserMapper.toEntity(userDTO);

        try {
            com.rev.app.dto.OrderDTO savedOrderDTO;
            if (productId != null) {
                // "Buy Now" flow
                savedOrderDTO = IOrderService.placeOrder(userDTO, productId, quantity, methodStr, deliveryAddress);
            } else {
                // Normal cart flow
                Optional<com.rev.app.dto.CartDTO> cartOpt = ICartService.getCartByUserId(user.getId());
                if (cartOpt.isEmpty() || cartOpt.get().getItems().isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMsg", "Cart is empty!");
                    return "redirect:/cart";
                }

                com.rev.app.dto.CartDTO cartDTO = cartOpt.get();
                savedOrderDTO = IOrderService.placeOrder(userDTO, cartDTO.getItems(), methodStr, deliveryAddress);
                ICartService.clearCart(user.getId());
            }

            redirectAttributes.addFlashAttribute("paymentMethod", savedOrderDTO.getPaymentMethod());
            redirectAttributes.addFlashAttribute("paymentStatus", savedOrderDTO.getPaymentStatus());
            redirectAttributes.addFlashAttribute("successMsg", "Order placed successfully! 🎊");

            return "redirect:/buyer/order-confirmation";
        } catch (com.rev.app.exception.InsufficientStockException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Order failed: " + e.getMessage());
            return "redirect:/checkout"
                    + (productId != null ? "?productId=" + productId + "&quantity=" + quantity : "");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "An unexpected error occurred during checkout: " + e.getMessage());
            return "redirect:/checkout"
                    + (productId != null ? "?productId=" + productId + "&quantity=" + quantity : "");
        }
    }
}
