package com.rev.app.controller;

import com.rev.app.config.TestSecurityConfig;
import com.rev.app.dto.ProductDTO;
import com.rev.app.entity.Category;
import com.rev.app.entity.Product;
import com.rev.app.entity.User;
import com.rev.app.security.CustomUserDetailsService;
import com.rev.app.security.JwtAuthenticationFilter;
import com.rev.app.security.JwtUtil;
import com.rev.app.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link ProductController}.
 * Uses @WebMvcTest to load only the web layer; all service deps are mocked.
 */
@WebMvcTest(controllers = ProductController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IProductService iProductService;
    @MockBean
    private IUserService iUserService;
    @MockBean
    private ICategoryService iCategoryService;
    @MockBean
    private IReviewService iReviewService;
    @MockBean
    private IFavoriteService iFavoriteService;
    @MockBean
    private IOrderService iOrderService;

    private User seller;
    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        seller = new User();
        seller.setId(1L);
        seller.setEmail("seller@test.com");

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100));
        product.setQuantity(10);
        product.setSeller(seller);

        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        product.setCategory(category);

        productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setName("Test Product");
        productDTO.setPrice(BigDecimal.valueOf(100));
        productDTO.setQuantity(10);
    }

    // ─── GET /products ────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /products - returns products view with product list")
    void viewProducts_returnsProductsView() throws Exception {
        when(iProductService.findAllDTOs()).thenReturn(List.of(productDTO));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attribute("isSellerView", false));
    }

    // ─── GET /products/seller ─────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "seller@test.com", roles = "SELLER")
    @DisplayName("GET /products/seller - returns seller's products")
    void viewSellerProducts_returnsSellerProducts() throws Exception {
        com.rev.app.dto.UserDTO sellerDTO = new com.rev.app.dto.UserDTO();
        sellerDTO.setId(1L);
        sellerDTO.setEmail("seller@test.com");

        when(iUserService.findByEmail("seller@test.com")).thenReturn(Optional.of(sellerDTO));
        when(iProductService.findBySellerId(1L)).thenReturn(List.of(productDTO));

        mockMvc.perform(get("/products/seller"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attribute("isSellerView", true))
                .andExpect(model().attributeExists("products"));
    }

    // ─── GET /products/add ────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "seller@test.com", roles = "SELLER")
    @DisplayName("GET /products/add - shows add product form")
    void showAddForm_returnsProductFormView() throws Exception {
        when(iCategoryService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/products/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-form"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attributeExists("categories"));
    }

    // ─── GET /products/{id} ───────────────────────────────────────────────────

    @Test
    @DisplayName("GET /products/{id} - returns product-detail view with product in model")
    void viewProduct_productExists_returnsDetailView() throws Exception {
        when(iProductService.findById(1L)).thenReturn(Optional.of(product));
        when(iFavoriteService.getFavoritesByUserId(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-detail"))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    @WithMockUser(username = "buyer@test.com")
    @DisplayName("GET /products/{id} - authenticated user sees isFav and canReview model attributes")
    void viewProduct_authenticated_addsUserSpecificModelAttributes() throws Exception {
        com.rev.app.dto.UserDTO buyerDTO = new com.rev.app.dto.UserDTO();
        buyerDTO.setId(2L);
        buyerDTO.setEmail("buyer@test.com");

        when(iProductService.findById(1L)).thenReturn(Optional.of(product));
        when(iUserService.findByEmail("buyer@test.com")).thenReturn(Optional.of(buyerDTO));
        when(iFavoriteService.getFavoritesByUserId(2L)).thenReturn(List.of());
        when(iOrderService.hasDeliveredOrderForProduct(2L, 1L)).thenReturn(false);
        when(iReviewService.findByProductIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-detail"))
                .andExpect(model().attributeExists("isFav"))
                .andExpect(model().attributeExists("canReview"));
    }

    // ─── POST /products/add ───────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "seller@test.com", roles = "SELLER")
    @DisplayName("POST /products/add - success redirects to /products/seller")
    void saveProduct_success_redirectsToSellerPage() throws Exception {
        mockMvc.perform(multipart("/products/add").with(csrf())
                .param("name", "New Product")
                .param("description", "A description")
                .param("price", "99.99")
                .param("categoryId", "1")
                .param("quantity", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/seller"));

        verify(iProductService).saveProduct(any(Product.class), eq("seller@test.com"), eq(1L));
    }

    // ─── GET /products/edit/{id} ──────────────────────────────────────────────

    @Test
    @WithMockUser(username = "seller@test.com", roles = "SELLER")
    @DisplayName("GET /products/edit/{id} - owner gets edit form")
    void showEditForm_owner_returnsEditForm() throws Exception {
        when(iProductService.findById(1L)).thenReturn(Optional.of(product));
        when(iCategoryService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/products/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-form"))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    @WithMockUser(username = "other@test.com", roles = "SELLER")
    @DisplayName("GET /products/edit/{id} - non-owner gets redirected with error")
    void showEditForm_nonOwner_redirectsWithError() throws Exception {
        when(iProductService.findById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/products/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/seller"));
    }

    // ─── GET /products/delete/{id} ────────────────────────────────────────────

    @Test
    @WithMockUser(username = "seller@test.com", roles = "SELLER")
    @DisplayName("GET /products/delete/{id} - owner deletes product and redirects")
    void deleteProduct_owner_deletesAndRedirects() throws Exception {
        when(iProductService.findById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/products/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/seller"));

        verify(iProductService).deleteProduct(1L);
    }

    @Test
    @WithMockUser(username = "other@test.com", roles = "SELLER")
    @DisplayName("GET /products/delete/{id} - non-owner gets redirected with error, no deletion")
    void deleteProduct_nonOwner_redirectsWithoutDeleting() throws Exception {
        when(iProductService.findById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/products/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/seller"));

        verify(iProductService, never()).deleteProduct(anyLong());
    }
}
