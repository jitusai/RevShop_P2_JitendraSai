package com.rev.app.service;

import com.rev.app.entity.User;

import java.util.Optional;

public interface UserService {

    User registerBuyer(User user);

    User registerSeller(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

}