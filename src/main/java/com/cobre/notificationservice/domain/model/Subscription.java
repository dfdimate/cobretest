package com.cobre.notificationservice.domain.model;

import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.EventType;

public record Subscription(
        ClientId clientId,
        EventType eventType,
        String targetUrl,
        boolean active) {
}

