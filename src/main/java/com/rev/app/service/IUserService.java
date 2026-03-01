package com.rev.app.service;

import com.rev.app.dto.UserDTO;
import java.util.Optional;

public interface IUserService {

    UserDTO registerBuyer(UserDTO userDto);

    UserDTO registerSeller(UserDTO userDto);

    /** Updates the user's address. Pass null to clear the address. */
    void updateAddress(Long userId, String address);

    Optional<UserDTO> findByEmail(String email);

    boolean existsByName(String name);

    Optional<UserDTO> findById(Long id);

    UserDTO saveUser(UserDTO userDto);
}