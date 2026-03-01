package com.rev.app.service;

import com.rev.app.dto.UserDTO;
import java.util.Optional;

public interface UserService {

    UserDTO registerBuyer(UserDTO userDto);

    UserDTO registerSeller(UserDTO userDto);

    /** Returns true if the buyer has a DELIVERED order containing this product. */
    boolean hasDeliveredOrderForProduct(Long buyerId, Long productId);

    /** Updates the user's address. Pass null to clear the address. */
    void updateAddress(Long userId, String address);

    Optional<UserDTO> findByEmail(String email);

    Optional<UserDTO> findById(Long id);

    UserDTO saveUser(UserDTO userDto);
}