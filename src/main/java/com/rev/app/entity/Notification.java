package com.rev.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notifications")
@SequenceGenerator(name = "notification_seq", sequenceName = "notification_seq", allocationSize = 1)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_seq")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String message;

    private boolean readStatus = false;
    private LocalDateTime createdAt;  // ✅ required for ORDER BY

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}