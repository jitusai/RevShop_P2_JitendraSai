package com.rev.app.repository;

import com.rev.app.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC")
    List<Notification> findByUserId(Long userId);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.readStatus = false")
    List<Notification> findUnreadNotifications(Long userId);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
}