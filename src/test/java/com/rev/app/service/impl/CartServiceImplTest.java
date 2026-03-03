package com.rev.app.service.impl;

import com.rev.app.dto.CartDTO;
import com.rev.app.entity.Cart;
import com.rev.app.entity.CartItem;
import com.rev.app.entity.Product;
import com.rev.app.entity.User;
import com.rev.app.repository.CartRepository;
import com.rev.app.repository.ProductRepository;
import com.rev.app.repository.UserRepository;
import com.rev.app.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CartServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private IUserService iUserService;

    @InjectMocks
    private CartServiceImpl cartService;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("buyer@test.com");

        testProduct = new Product();
        testProduct.setId(10L);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(200.00));
    }

    // ─── addItemToCart: no existing cart ─────────────────────────────────────

    @Test
    @DisplayName("addItemToCart - creates new cart when user has no cart, adds item")
    void addItemToCart_noExistingCart_createsCartAndAddsItem() {
        Cart newCart = new Cart();
        newCart.setId(1L);
        newCart.setUser(testUser);
        newCart.setItems(new ArrayList<>());

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));

        cartService.addItemToCart(1L, 10L, 2);

        // save called twice: once for cart creation, once after adding item
        verify(cartRepository, times(2)).save(any(Cart.class));
    }

    // ─── addItemToCart: existing cart, new product item ──────────────────────

    @Test
    @DisplayName("addItemToCart - existing cart with different product adds new CartItem")
    void addItemToCart_existingCart_newProduct_addsNewItem() {
        Cart existingCart = new Cart();
        existingCart.setId(1L);
        existingCart.setUser(testUser);
        existingCart.setItems(new ArrayList<>());

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(existingCart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

        cartService.addItemToCart(1L, 10L, 3);

        assertEquals(1, existingCart.getItems().size());
        assertEquals(3, existingCart.getItems().get(0).getQuantity());
        verify(cartRepository).save(existingCart);
    }

    // ─── addItemToCart: existing cart, same product increments quantity ───────

    @Test
    @DisplayName("addItemToCart - same product in cart increments quantity")
    void addItemToCart_existingCart_sameProduct_incrementsQuantity() {
        CartItem existingItem = CartItem.builder()
                .product(testProduct)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(400.00))
                .build();

        Cart existingCart = new Cart();
        existingCart.setId(1L);
        existingCart.setUser(testUser);
        existingCart.setItems(new ArrayList<>());
        existingCart.getItems().add(existingItem);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(existingCart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

        cartService.addItemToCart(1L, 10L, 3);

        // Quantity should be 2 + 3 = 5
        assertEquals(5, existingItem.getQuantity());
        verify(cartRepository).save(existingCart);
    }

    // ─── addItemToCart: discounted price used when available ─────────────────

    @Test
    @DisplayName("addItemToCart - uses discounted price for total when available")
    void addItemToCart_withDiscountedPrice_usesDiscountedPrice() {
        testProduct.setDiscountedPrice(BigDecimal.valueOf(150.00));

        Cart existingCart = new Cart();
        existingCart.setId(1L);
        existingCart.setUser(testUser);
        existingCart.setItems(new ArrayList<>());

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(existingCart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

        cartService.addItemToCart(1L, 10L, 2);

        // total = 150 * 2 = 300
        assertEquals(0, BigDecimal.valueOf(300.00).compareTo(existingCart.getItems().get(0).getTotalPrice()));
    }

    // ─── addItemToCart: product not found throws exception ────────────────────

    @Test
    @DisplayName("addItemToCart - throws RuntimeException when product not found")
    void addItemToCart_productNotFound_throwsException() {
        Cart existingCart = new Cart();
        existingCart.setId(1L);
        existingCart.setItems(new ArrayList<>());

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(existingCart));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> cartService.addItemToCart(1L, 99L, 1));
    }

    // ─── removeItemFromCart ───────────────────────────────────────────────────

    @Test
    @DisplayName("removeItemFromCart - removes item by cartItemId and saves")
    void removeItemFromCart_removesItemAndSaves() {
        CartItem item1 = CartItem.builder().product(testProduct).quantity(1).totalPrice(BigDecimal.valueOf(200))
                .build();
        item1.setId(101L);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setItems(new ArrayList<>());
        cart.getItems().add(item1);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.removeItemFromCart(1L, 101L);

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    // ─── getCartByUserId ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getCartByUserId - returns CartDTO when cart exists")
    void getCartByUserId_cartExists_returnsCartDTO() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(testUser);
        cart.setItems(new ArrayList<>());

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        Optional<CartDTO> result = cartService.getCartByUserId(1L);

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("getCartByUserId - returns empty Optional when no cart")
    void getCartByUserId_noCart_returnsEmpty() {
        when(cartRepository.findByUserId(2L)).thenReturn(Optional.empty());

        Optional<CartDTO> result = cartService.getCartByUserId(2L);

        assertFalse(result.isPresent());
    }

    // ─── clearCart ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("clearCart - clears all items in user cart and saves")
    void clearCart_clearsAndSaves() {
        CartItem item = CartItem.builder().product(testProduct).quantity(1).totalPrice(BigDecimal.TEN).build();
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setItems(new ArrayList<>());
        cart.getItems().add(item);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.clearCart(1L);

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }
}
