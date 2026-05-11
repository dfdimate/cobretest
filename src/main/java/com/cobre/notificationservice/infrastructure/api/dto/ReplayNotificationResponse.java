package com.cobre.notificationservice.infrastructure.api.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ReplayNotificationResponse(
        String notificationEventId,
        String deliveryStatus,
        String message) {
}

