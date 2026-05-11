package com.cobre.notificationservice.application.usecase;

public class NotificationEventNotFoundException extends RuntimeException {

    public NotificationEventNotFoundException(String notificationEventId) {
        super("Notification event not found: " + notificationEventId);
    }
}

