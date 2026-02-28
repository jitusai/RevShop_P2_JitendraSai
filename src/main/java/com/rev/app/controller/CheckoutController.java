package com.rev.app.controller;

import com.rev.app.entity.*;
import com.rev.app.entity.enums.OrderStatus;
import com.rev.app.entity.enums.PaymentMethod;
import com.rev.app.entity.enums.PaymentStatus;
import com.rev.app.service.CartService;
import com.rev.app.service.OrderService;
import com.rev.app.service.UserService;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;

    @GetMapping
    public String showCheckout(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<User> userOpt = userService.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty())
            return "redirect:/login";

        User user = userOpt.get();
        Optional<Cart> cartOpt = cartService.getCartByUserId(user.getId());

        if (cartOpt.isEmpty() || cartOpt.get().getItems().isEmpty()) {
            model.addAttribute("error", "Your cart is empty!");
            model.addAttribute("cart", null);
        } else {
            Cart cart = cartOpt.get();
            BigDecimal total = cart.getItems().stream()
                    .map(CartItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("cart", cart);
            model.addAttribute("cartTotal", total);
        }
        model.addAttribute("user", user);
        return "checkout";
    }

    @PostMapping("/place")
    public String placeOrder(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("paymentMethod") String methodStr,
            @RequestParam("address") String deliveryAddress,
            RedirectAttributes redirectAttributes) {

        Optional<User> userOpt = userService.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty())
            return "redirect:/login";

        User user = userOpt.get();

        Optional<Cart> cartOpt = cartService.getCartByUserId(user.getId());
        if (cartOpt.isEmpty() || cartOpt.get().getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Cart is empty!");
            return "redirect:/cart";
        }

        Cart cart = cartOpt.get();
        BigDecimal total = cart.getItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build order
        Order order = new Order();
        order.setBuyer(user);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(total);

        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> OrderItem.builder()
                .order(order)
                .product(cartItem.getProduct())
                .quantity(cartItem.getQuantity())
                .price(cartItem.getProduct().getDiscountedPrice() != null
                        ? cartItem.getProduct().getDiscountedPrice()
                        : cartItem.getProduct().getPrice())
                .build()).collect(Collectors.toList());

        order.setItems(orderItems);

        // Payment
        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(methodStr);
        } catch (IllegalArgumentException e) {
            method = PaymentMethod.CASH_ON_DELIVERY; // Safe fallback
        }

        PaymentStatus paymentStatus = (method == PaymentMethod.CASH_ON_DELIVERY)
                ? PaymentStatus.INITIATED
                : PaymentStatus.SUCCESS;

        Payment payment = Payment.builder()
                .order(order)
                .method(method)
                .status(paymentStatus)
                .amount(total)
                .build();
        order.setPayment(payment);

        // Save address if updated
        if (deliveryAddress != null && !deliveryAddress.isBlank()) {
            user.setAddress(deliveryAddress);
            userService.saveUser(user);
        }

        // Place order
        orderService.placeOrder(order);

        // ── Clear cart using dedicated service method ──────────────────────
        cartService.clearCart(user.getId());

        // Pass payment info to confirmation page
        redirectAttributes.addFlashAttribute("paymentMethod", method.name());
        redirectAttributes.addFlashAttribute("paymentStatus", paymentStatus.name());

        return "redirect:/buyer/order-confirmation";
    }
}
