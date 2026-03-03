package com.rev.app.rest;

import com.rev.app.dto.CartDTO;
import com.rev.app.service.ICartService;
import com.rev.app.service.IProductService;
import com.rev.app.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartRestController {

    private final ICartService ICartService;
    private final IUserService IUserService;
    private final IProductService IProductService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<CartDTO> getCartByUserId(@PathVariable Long userId) {
        return ICartService.getCartByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addToCart(@RequestParam Long userId, @RequestParam Long productId,
            @RequestParam int quantity) {
        ICartService.addItemToCart(userId, productId, quantity);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove/{userId}/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long userId, @PathVariable Long cartItemId) {
        ICartService.removeItemFromCart(userId, cartItemId);
        return ResponseEntity.ok().build();
    }
}
