package com.rev.app.service;

import com.rev.app.dto.UserDTO;
import com.rev.app.entity.User;
import com.rev.app.entity.enums.Role;
import com.rev.app.repository.UserRepository;
import com.rev.app.service.impl.UserServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceJUnit4Test {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterBuyer_Success() {
        UserDTO inputDto = new UserDTO();
        inputDto.setName("John Doe");
        inputDto.setEmail("john@example.com");
        inputDto.setPassword("password123");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("John Doe");
        savedUser.setEmail("john@example.com");
        savedUser.setRole(Role.ROLE_BUYER);

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDTO result = userService.registerBuyer(inputDto);

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals(Role.ROLE_BUYER.name(), result.getRole());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testFindByEmail_Found() {
        User user = new User();
        user.setId(1L);
        user.setEmail("john@example.com");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        Optional<UserDTO> result = userService.findByEmail("john@example.com");

        assertTrue(result.isPresent());
        assertEquals("john@example.com", result.get().getEmail());
    }

    @Test
    public void testExistsByName_True() {
        User user = new User();
        user.setName("John Doe");

        when(userRepository.findByName("John Doe")).thenReturn(Optional.of(user));

        boolean result = userService.existsByName("John Doe");

        assertTrue(result);
    }
}
