package com.rev.app.service;

import com.rev.app.entity.Notification;

import java.util.List;

public interface INotificationService {

    void sendNotification(Notification notification);

    List<Notification> getUserNotifications(Long userId);

    void markAsRead(Long notificationId);
}
