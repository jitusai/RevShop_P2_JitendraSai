package com.rev.app.service;

import com.rev.app.dto.UserDTO;
import com.rev.app.entity.User;
import com.rev.app.entity.enums.Role;
import com.rev.app.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class IUserServiceTest {

    @Autowired
    private IUserService IUserService;

    @Test
    void testRegisterBuyer() {

        User user = User.builder()
                .name("Service Test")
                .email("service@test.com")
                .password("12345")
                .enabled(true)
                .build();

        UserDTO userDTO = UserMapper.toDTO(user);
        userDTO.setPassword("12345"); // DTO doesn't store password from entity in this mapper

        UserDTO saved = IUserService.registerBuyer(userDTO);

        assertNotNull(saved.getId());
        assertEquals(Role.ROLE_BUYER.name(), saved.getRole());
    }
}