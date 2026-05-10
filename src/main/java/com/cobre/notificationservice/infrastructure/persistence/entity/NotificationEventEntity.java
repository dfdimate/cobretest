package com.cobre.notificationservice.infrastructure.persistence.entity;

import com.cobre.notificationservice.domain.model.value.DeliveryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "notification_events")
public class NotificationEventEntity {

    @Id
    @Column(name = "notification_event_id", nullable = false, length = 64)
    private String notificationEventId;

    @Column(name = "source_event_id", nullable = false, length = 64)
    private String sourceEventId;

    @Column(name = "client_id", nullable = false, length = 64)
    private String clientId;

    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "event_created_at", nullable = false)
    private Instant eventCreatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 32)
    private DeliveryStatus deliveryStatus;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "final_failure_reason", length = 512)
    private String finalFailureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "delivery_claimed_at")
    private Instant deliveryClaimedAt;

    public NotificationEventEntity() {
    }

    public String getNotificationEventId() {
        return notificationEventId;
    }

    public void setNotificationEventId(String notificationEventId) {
        this.notificationEventId = notificationEventId;
    }

    public String getSourceEventId() {
        return sourceEventId;
    }

    public void setSourceEventId(String sourceEventId) {
        this.sourceEventId = sourceEventId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getEventCreatedAt() {
        return eventCreatedAt;
    }

    public void setEventCreatedAt(Instant eventCreatedAt) {
        this.eventCreatedAt = eventCreatedAt;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public Instant getLastAttemptAt() {
        return lastAttemptAt;
    }

    public void setLastAttemptAt(Instant lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public Instant getNextRetryAt() {
        return nextRetryAt;
    }

    public void setNextRetryAt(Instant nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getFinalFailureReason() {
        return finalFailureReason;
    }

    public void setFinalFailureReason(String finalFailureReason) {
        this.finalFailureReason = finalFailureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getDeliveryClaimedAt() {
        return deliveryClaimedAt;
    }

    public void setDeliveryClaimedAt(Instant deliveryClaimedAt) {
        this.deliveryClaimedAt = deliveryClaimedAt;
    }
}
