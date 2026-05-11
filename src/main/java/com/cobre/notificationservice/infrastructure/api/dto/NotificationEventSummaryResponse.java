package com.cobre.notificationservice.infrastructure.api.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.Instant;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record NotificationEventSummaryResponse(
        String notificationEventId,
        String eventId,
        String eventType,
        String content,
        Instant eventCreatedAt,
        String deliveryStatus,
        Instant deliveredAt,
        Instant lastAttemptAt) {
}

