package com.cobre.notificationservice.domain.model;

import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.DeliveryStatus;
import com.cobre.notificationservice.domain.model.value.EventType;
import com.cobre.notificationservice.domain.model.value.NotificationEventId;
import com.cobre.notificationservice.domain.model.value.SourceEventId;
import java.time.Instant;

public class NotificationEvent {

    private final NotificationEventId notificationEventId;
    private final SourceEventId sourceEventId;
    private final ClientId clientId;
    private final EventType eventType;
    private final String content;
    private final Instant eventCreatedAt;
    private final Instant createdAt;
    private DeliveryStatus deliveryStatus;
    private int attemptCount;
    private Instant lastAttemptAt;
    private Instant deliveredAt;
    private Instant nextRetryAt;
    private Integer httpStatus;
    private String finalFailureReason;

    private NotificationEvent(
            NotificationEventId notificationEventId,
            SourceEventId sourceEventId,
            ClientId clientId,
            EventType eventType,
            String content,
            Instant eventCreatedAt,
            Instant createdAt,
            DeliveryStatus deliveryStatus,
            int attemptCount,
            Instant lastAttemptAt,
            Instant deliveredAt,
            Instant nextRetryAt,
            Integer httpStatus,
            String finalFailureReason) {
        this.notificationEventId = notificationEventId;
        this.sourceEventId = sourceEventId;
        this.clientId = clientId;
        this.eventType = eventType;
        this.content = content;
        this.eventCreatedAt = eventCreatedAt;
        this.createdAt = createdAt;
        this.deliveryStatus = deliveryStatus;
        this.attemptCount = attemptCount;
        this.lastAttemptAt = lastAttemptAt;
        this.deliveredAt = deliveredAt;
        this.nextRetryAt = nextRetryAt;
        this.httpStatus = httpStatus;
        this.finalFailureReason = finalFailureReason;
    }

    public static NotificationEvent pending(
            NotificationEventId notificationEventId,
            SourceEventId sourceEventId,
            ClientId clientId,
            EventType eventType,
            String content,
            Instant eventCreatedAt,
            Instant createdAt) {
        return new NotificationEvent(
                notificationEventId,
                sourceEventId,
                clientId,
                eventType,
                content,
                eventCreatedAt,
                createdAt,
                DeliveryStatus.PENDING,
                0,
                null,
                null,
                null,
                null,
                null);
    }

    public static NotificationEvent restore(
            NotificationEventId notificationEventId,
            SourceEventId sourceEventId,
            ClientId clientId,
            EventType eventType,
            String content,
            Instant eventCreatedAt,
            Instant createdAt,
            DeliveryStatus deliveryStatus,
            int attemptCount,
            Instant lastAttemptAt,
            Instant deliveredAt,
            Instant nextRetryAt,
            Integer httpStatus,
            String finalFailureReason) {
        return new NotificationEvent(
                notificationEventId,
                sourceEventId,
                clientId,
                eventType,
                content,
                eventCreatedAt,
                createdAt,
                deliveryStatus,
                attemptCount,
                lastAttemptAt,
                deliveredAt,
                nextRetryAt,
                httpStatus,
                finalFailureReason);
    }

    public static NotificationEvent failed(
            NotificationEventId notificationEventId,
            SourceEventId sourceEventId,
            ClientId clientId,
            EventType eventType,
            String content,
            Instant eventCreatedAt,
            Instant createdAt,
            Instant lastAttemptAt,
            Integer httpStatus,
            String finalFailureReason,
            int attemptCount) {
        return new NotificationEvent(
                notificationEventId,
                sourceEventId,
                clientId,
                eventType,
                content,
                eventCreatedAt,
                createdAt,
                DeliveryStatus.FAILED,
                attemptCount,
                lastAttemptAt,
                null,
                null,
                httpStatus,
                finalFailureReason);
    }

    public void markDelivering(Instant attemptedAt) {
        this.deliveryStatus = DeliveryStatus.DELIVERING;
        this.lastAttemptAt = attemptedAt;
        this.attemptCount++;
        this.nextRetryAt = null;
    }

    public void markCompleted(Instant deliveredAt, int httpStatus) {
        this.deliveryStatus = DeliveryStatus.COMPLETED;
        this.deliveredAt = deliveredAt;
        this.lastAttemptAt = deliveredAt;
        this.nextRetryAt = null;
        this.httpStatus = httpStatus;
        this.finalFailureReason = null;
    }

    public void markRetryableFailure(
            Instant attemptedAt,
            Integer httpStatus,
            String finalFailureReason,
            Instant nextRetryAt) {
        this.deliveryStatus = DeliveryStatus.FAILED_RETRYABLE;
        this.lastAttemptAt = attemptedAt;
        this.httpStatus = httpStatus;
        this.finalFailureReason = finalFailureReason;
        this.nextRetryAt = nextRetryAt;
    }

    public void markFailed(Instant attemptedAt, Integer httpStatus, String finalFailureReason) {
        this.deliveryStatus = DeliveryStatus.FAILED;
        this.lastAttemptAt = attemptedAt;
        this.httpStatus = httpStatus;
        this.finalFailureReason = finalFailureReason;
        this.nextRetryAt = null;
    }

    public boolean hasRetriesRemaining(int maxAttempts) {
        return attemptCount < maxAttempts;
    }

    public boolean isReadyForDelivery() {
        return deliveryStatus == DeliveryStatus.PENDING || deliveryStatus == DeliveryStatus.FAILED_RETRYABLE;
    }

    public boolean canReplay() {
        return deliveryStatus == DeliveryStatus.FAILED;
    }

    public NotificationEventId notificationEventId() {
        return notificationEventId;
    }

    public SourceEventId sourceEventId() {
        return sourceEventId;
    }

    public ClientId clientId() {
        return clientId;
    }

    public EventType eventType() {
        return eventType;
    }

    public String content() {
        return content;
    }

    public Instant eventCreatedAt() {
        return eventCreatedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public DeliveryStatus deliveryStatus() {
        return deliveryStatus;
    }

    public int attemptCount() {
        return attemptCount;
    }

    public Instant lastAttemptAt() {
        return lastAttemptAt;
    }

    public Instant deliveredAt() {
        return deliveredAt;
    }

    public Integer httpStatus() {
        return httpStatus;
    }

    public String finalFailureReason() {
        return finalFailureReason;
    }

    public Instant nextRetryAt() {
        return nextRetryAt;
    }
}
