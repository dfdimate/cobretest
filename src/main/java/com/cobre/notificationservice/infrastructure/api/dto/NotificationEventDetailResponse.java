package com.cobre.notificationservice.infrastructure.api.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.Instant;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record NotificationEventDetailResponse(
        String notificationEventId,
        String eventId,
        String eventType,
        String content,
        Instant eventCreatedAt,
        String deliveryStatus,
        Instant deliveredAt,
        Instant lastAttemptAt,
        int attemptCount,
        Integer httpStatus,
        String failureReason) {
}

