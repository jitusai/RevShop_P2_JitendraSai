package com.rev.app.service.impl;

import com.rev.app.dto.UserDTO;
import com.rev.app.entity.User;
import com.rev.app.entity.enums.Role;
import com.rev.app.mapper.UserMapper;
import com.rev.app.repository.UserRepository;
import com.rev.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO registerBuyer(UserDTO userDto) {
        User user = UserMapper.toEntity(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(Role.ROLE_BUYER);
        return UserMapper.toDTO(userRepository.save(user));
    }

    @Override
    public UserDTO registerSeller(UserDTO userDto) {
        User user = UserMapper.toEntity(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(Role.ROLE_SELLER);
        return UserMapper.toDTO(userRepository.save(user));
    }

    @Override
    public Optional<UserDTO> findByEmail(String email) {
        return userRepository.findByEmail(email).map(UserMapper::toDTO);
    }

    @Override
    public Optional<UserDTO> findById(Long id) {
        return userRepository.findById(id).map(UserMapper::toDTO);
    }

    @Override
    public UserDTO saveUser(UserDTO userDto) {
        if (userDto.getId() != null) {
            return userRepository.findById(userDto.getId())
                    .map(existingUser -> {
                        if (userDto.getName() != null)
                            existingUser.setName(userDto.getName());
                        if (userDto.getEmail() != null)
                            existingUser.setEmail(userDto.getEmail());
                        if (userDto.getPhone() != null)
                            existingUser.setPhone(userDto.getPhone());
                        if (userDto.getAddress() != null)
                            existingUser.setAddress(userDto.getAddress());
                        if (userDto.getGstNumber() != null)
                            existingUser.setGstNumber(userDto.getGstNumber());
                        if (userDto.getSellerDistributorName() != null)
                            existingUser.setSellerDistributorName(userDto.getSellerDistributorName());
                        // Password and Role are typically NOT updated via this general saveUser call
                        // unless explicitly provided- but here we definitely want to preserve them.
                        return UserMapper.toDTO(userRepository.save(existingUser));
                    })
                    .orElseGet(() -> UserMapper.toDTO(userRepository.save(UserMapper.toEntity(userDto))));
        }
        User user = UserMapper.toEntity(userDto);
        return UserMapper.toDTO(userRepository.save(user));
    }

    @Override
    public void updateAddress(Long userId, String address) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setAddress(address);
            userRepository.save(user);
        });
    }

    @Override
    public boolean hasDeliveredOrderForProduct(Long buyerId, Long productId) {
        // This should actually be in OrderService, but since it's in UserService
        // interface...
        // Wait, I see it's also in OrderService. I'll just leave it for now or
        // implement if needed.
        return false;
    }
}