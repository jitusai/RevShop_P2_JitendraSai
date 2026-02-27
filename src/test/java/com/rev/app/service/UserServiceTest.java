package com.rev.app.service;

import com.rev.app.entity.User;
import com.rev.app.entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testRegisterBuyer() {

        User user = User.builder()
                .name("Service Test")
                .email("service@test.com")
                .password("12345")
                .enabled(true)
                .build();

        User saved = userService.registerBuyer(user);

        assertNotNull(saved.getId());
        assertEquals(Role.ROLE_BUYER, saved.getRole());
    }
}