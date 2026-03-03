package com.rev.app.rest;

import com.rev.app.entity.Notification;
import com.rev.app.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationRestController {

    private final INotificationService INotificationService;

    @GetMapping("/user/{userId}")
    public List<Notification> getUserNotifications(@PathVariable Long userId) {
        return INotificationService.getUserNotifications(userId);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        INotificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}
