package com.cobre.notificationservice.domain.model.value;

public record NotificationEventId(String value) {

    public NotificationEventId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("notificationEventId cannot be blank");
        }
    }
}

