package com.rev.app.repository;

import com.rev.app.entity.User;
import com.rev.app.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(Role role);

    @Query("SELECT u FROM User u WHERE u.enabled = true")
    List<User> findActiveUsers();
}