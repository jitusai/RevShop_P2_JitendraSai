package com.rev.app.service;

import com.rev.app.entity.Notification;

import java.util.List;

public interface NotificationService {

    void sendNotification(Notification notification);

    List<Notification> getUserNotifications(Long userId);
}