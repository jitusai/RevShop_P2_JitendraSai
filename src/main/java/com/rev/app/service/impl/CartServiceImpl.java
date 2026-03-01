package com.rev.app.service.impl;

import com.rev.app.entity.Cart;
import com.rev.app.entity.CartItem;
import com.rev.app.entity.Product;
import com.rev.app.entity.User;
import com.rev.app.repository.CartRepository;
import com.rev.app.repository.ProductRepository;
import com.rev.app.repository.UserRepository;
import com.rev.app.service.ICartService;
import com.rev.app.service.IUserService;
import com.rev.app.dto.CartDTO;
import com.rev.app.mapper.CartMapper;
import com.rev.app.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final IUserService IUserService;

    @Override
    public CartDTO createCart(CartDTO cartDTO) {
        User user = IUserService.findById(cartDTO.getUserId())
                .map(UserMapper::toEntity)
                .orElse(null);
        Cart cart = CartMapper.toEntity(cartDTO);
        cart.setUser(user);
        return CartMapper.toDTO(cartRepository.save(cart));
    }

    @Override
    public Optional<CartDTO> getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId).map(CartMapper::toDTO);
    }

    @Override
    public CartDTO saveCart(CartDTO cartDTO) {
        User user = IUserService.findById(cartDTO.getUserId())
                .map(UserMapper::toEntity)
                .orElse(null);
        Cart cart = CartMapper.toEntity(cartDTO);
        cart.setUser(user);
        return CartMapper.toDTO(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public void addItemToCart(Long userId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = new Cart();
            userRepository.findById(userId).ifPresent(newCart::setUser);
            newCart.setItems(new ArrayList<>());
            return cartRepository.save(newCart);
        });

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            existingItem.setTotalPrice(product.getDiscountedPrice() != null
                    ? product.getDiscountedPrice().multiply(BigDecimal.valueOf(existingItem.getQuantity()))
                    : product.getPrice().multiply(BigDecimal.valueOf(existingItem.getQuantity())));
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .totalPrice(product.getDiscountedPrice() != null
                            ? product.getDiscountedPrice().multiply(BigDecimal.valueOf(quantity))
                            : product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                    .build();
            cart.getItems().add(newItem);
        }
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void updateItemQuantity(Long userId, Long cartItemId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .ifPresent(item -> {
                    item.setQuantity(quantity);
                    BigDecimal unitPrice = item.getProduct().getDiscountedPrice() != null
                            ? item.getProduct().getDiscountedPrice()
                            : item.getProduct().getPrice();
                    item.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity)));
                });
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void removeItemFromCart(Long userId, Long cartItemId) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }
}