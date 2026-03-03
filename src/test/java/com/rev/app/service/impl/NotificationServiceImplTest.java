package com.rev.app.service.impl;

import com.rev.app.entity.Notification;
import com.rev.app.entity.User;
import com.rev.app.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link NotificationServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@test.com");
    }

    // ─── sendNotification ─────────────────────────────────────────────────────

    @Test
    @DisplayName("sendNotification - saves notification via repository")
    void sendNotification_savesNotification() {
        Notification notification = Notification.builder()
                .user(testUser)
                .message("Your order has been placed.")
                .build();

        notificationService.sendNotification(notification);

        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    @DisplayName("sendNotification - saved object matches the given notification")
    void sendNotification_savedObjectMatchesInput() {
        Notification notification = Notification.builder()
                .user(testUser)
                .message("Low stock alert for Product X")
                .build();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        notificationService.sendNotification(notification);
        verify(notificationRepository).save(captor.capture());

        assertEquals("Low stock alert for Product X", captor.getValue().getMessage());
    }

    // ─── getUserNotifications ─────────────────────────────────────────────────

    @Test
    @DisplayName("getUserNotifications - returns list from repository")
    void getUserNotifications_returnsListFromRepo() {
        Notification n1 = Notification.builder().user(testUser).message("Msg 1").build();
        Notification n2 = Notification.builder().user(testUser).message("Msg 2").build();
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(n1, n2));

        List<Notification> result = notificationService.getUserNotifications(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Msg 1", result.get(0).getMessage());
    }

    @Test
    @DisplayName("getUserNotifications - returns empty list when user has no notifications")
    void getUserNotifications_noNotifications_returnsEmptyList() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(99L)).thenReturn(List.of());

        List<Notification> result = notificationService.getUserNotifications(99L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ─── markAsRead ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("markAsRead - sets readStatus to true and saves when notification found")
    void markAsRead_notificationExists_setsReadStatusAndSaves() {
        Notification notification = Notification.builder()
                .user(testUser)
                .message("Test notification")
                .readStatus(false)
                .build();
        notification.setId(5L);
        notification.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.findById(5L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.markAsRead(5L);

        assertTrue(notification.isReadStatus());
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("markAsRead - does nothing when notification not found")
    void markAsRead_notificationNotFound_doesNothing() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        notificationService.markAsRead(999L);

        verify(notificationRepository, never()).save(any());
    }
}
