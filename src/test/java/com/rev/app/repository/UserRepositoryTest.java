package com.rev.app.repository;

import com.rev.app.entity.User;
import com.rev.app.entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindUser() {

        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("12345")
                .role(Role.ROLE_BUYER)
                .enabled(true)
                .build();

        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals(Role.ROLE_BUYER, foundUser.get().getRole());
    }
}