package com.rev.app.controller;

import com.rev.app.config.TestSecurityConfig;
import com.rev.app.dto.UserDTO;
import com.rev.app.security.CustomUserDetailsService;
import com.rev.app.security.JwtAuthenticationFilter;
import com.rev.app.security.JwtUtil;
import com.rev.app.service.IUserService;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link UserController}.
 * Uses @WebMvcTest to load only the web layer and mocks IUserService.
 */
@WebMvcTest(controllers = UserController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IUserService iUserService;

    // ─── GET /register ───────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /register - shows registration form with empty UserDTO")
    void showRegistrationForm_returnsRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    // ─── POST /register - duplicate email ────────────────────────────────────

    @Test
    @DisplayName("POST /register - duplicate email stays on form with error")
    void registerUser_duplicateEmail_staysOnForm() throws Exception {
        UserDTO existingUser = new UserDTO();
        existingUser.setEmail("existing@test.com");
        when(iUserService.findByEmail("existing@test.com")).thenReturn(Optional.of(existingUser));

        mockMvc.perform(post("/register").with(csrf())
                .param("email", "existing@test.com")
                .param("name", "NewUser")
                .param("password", "pass123")
                .param("role", "ROLE_BUYER"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("errorMsg"));

        verify(iUserService, never()).registerBuyer(any());
        verify(iUserService, never()).registerSeller(any());
    }

    // ─── POST /register - duplicate name ─────────────────────────────────────

    @Test
    @DisplayName("POST /register - duplicate name stays on form with 'choose another name'")
    void registerUser_duplicateName_staysOnForm() throws Exception {
        when(iUserService.findByEmail(anyString())).thenReturn(Optional.empty());
        when(iUserService.existsByName("TakenName")).thenReturn(true);

        mockMvc.perform(post("/register").with(csrf())
                        .param("email", "new@test.com")
                        .param("name", "TakenName")
                        .param("password", "pass123")
                        .param("role", "ROLE_BUYER"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("errorMsg", "choose another name"));

        verify(iUserService, never()).registerBuyer(any());
    }

    // ─── POST /register - success as buyer ───────────────────────────────────

    @Test
    @DisplayName("POST /register - success (buyer) redirects to /login")
    void registerUser_successBuyer_redirectsToLogin() throws Exception {
        when(iUserService.findByEmail(anyString())).thenReturn(Optional.empty());
        when(iUserService.existsByName(anyString())).thenReturn(false);

        mockMvc.perform(post("/register").with(csrf())
                        .param("email", "buyer@test.com")
                        .param("name", "BuyerUser")
                        .param("password", "pass123")
                        .param("role", "ROLE_BUYER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(iUserService).registerBuyer(any(UserDTO.class));
        verify(iUserService, never()).registerSeller(any());
    }

    // ─── POST /register - success as seller ──────────────────────────────────

    @Test
    @DisplayName("POST /register - success (seller) calls registerSeller and redirects")
    void registerUser_successSeller_callsRegisterSeller() throws Exception {
        when(iUserService.findByEmail(anyString())).thenReturn(Optional.empty());
        when(iUserService.existsByName(anyString())).thenReturn(false);

        mockMvc.perform(post("/register").with(csrf())
                        .param("email", "seller@test.com")
                        .param("name", "SellerUser")
                        .param("password", "pass123")
                        .param("role", "ROLE_SELLER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(iUserService).registerSeller(any(UserDTO.class));
        verify(iUserService, never()).registerBuyer(any());
    }

    // ─── GET /login ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /login - returns login view")
    void showLoginForm_returnsLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    // ─── GET /profile ─────────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("GET /profile (authenticated) - returns profile view with user in model")
    void viewProfile_authenticated_returnsProfileView() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("user@test.com");
        when(iUserService.findByEmail("user@test.com")).thenReturn(Optional.of(userDTO));

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"));
    }

    // ─── POST /profile/address/update ─────────────────────────────────────────

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("POST /profile/address/update - calls updateAddress and redirects")
    void updateAddress_callsServiceAndRedirects() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setEmail("user@test.com");
        when(iUserService.findByEmail("user@test.com")).thenReturn(Optional.of(userDTO));

        mockMvc.perform(post("/profile/address/update").with(csrf())
                .param("address", "123 New Street, City"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?success_edit"));

        verify(iUserService).updateAddress(1L, "123 New Street, City");
    }
}
