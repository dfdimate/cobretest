package com.cobre.notificationservice.domain.model.value;

public record EventType(String value) {

    public EventType {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("eventType cannot be blank");
        }
    }
}

