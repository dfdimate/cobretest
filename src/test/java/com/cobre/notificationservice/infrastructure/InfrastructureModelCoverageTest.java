package com.cobre.notificationservice.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cobre.notificationservice.application.port.out.DeliveryResult;
import com.cobre.notificationservice.application.usecase.NotificationEventNotFoundException;
import com.cobre.notificationservice.application.usecase.ReplayNotAllowedException;
import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.DeliveryStatus;
import com.cobre.notificationservice.domain.model.value.EventType;
import com.cobre.notificationservice.domain.model.value.NotificationEventId;
import com.cobre.notificationservice.domain.model.value.SourceEventId;
import com.cobre.notificationservice.infrastructure.api.dto.NotificationEventDetailResponse;
import com.cobre.notificationservice.infrastructure.api.dto.NotificationEventSummaryResponse;
import com.cobre.notificationservice.infrastructure.api.dto.NotificationEventsResponse;
import com.cobre.notificationservice.infrastructure.api.dto.ReplayNotificationResponse;
import com.cobre.notificationservice.infrastructure.persistence.entity.NotificationEventEntity;
import com.cobre.notificationservice.infrastructure.persistence.entity.SubscriptionEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InfrastructureModelCoverageTest {

    @Test
    void shouldReadAndWriteNotificationEventEntityFields() {
        NotificationEventEntity entity = new NotificationEventEntity();
        Instant now = Instant.parse("2024-03-15T09:30:22Z");
        entity.setNotificationEventId("EVT001");
        entity.setSourceEventId("EVT001");
        entity.setClientId("CLIENT001");
        entity.setEventType("credit_card_payment");
        entity.setContent("Credit card payment received for $150.00");
        entity.setEventCreatedAt(now);
        entity.setDeliveryStatus(DeliveryStatus.FAILED);
        entity.setAttemptCount(4);
        entity.setLastAttemptAt(now);
        entity.setDeliveredAt(now);
        entity.setNextRetryAt(now);
        entity.setHttpStatus(503);
        entity.setFinalFailureReason("failure");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeliveryClaimedAt(now);

        assertThat(entity.getNotificationEventId()).isEqualTo("EVT001");
        assertThat(entity.getSourceEventId()).isEqualTo("EVT001");
        assertThat(entity.getClientId()).isEqualTo("CLIENT001");
        assertThat(entity.getEventType()).isEqualTo("credit_card_payment");
        assertThat(entity.getContent()).contains("$150.00");
        assertThat(entity.getEventCreatedAt()).isEqualTo(now);
        assertThat(entity.getDeliveryStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(entity.getAttemptCount()).isEqualTo(4);
        assertThat(entity.getLastAttemptAt()).isEqualTo(now);
        assertThat(entity.getDeliveredAt()).isEqualTo(now);
        assertThat(entity.getNextRetryAt()).isEqualTo(now);
        assertThat(entity.getHttpStatus()).isEqualTo(503);
        assertThat(entity.getFinalFailureReason()).isEqualTo("failure");
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
        assertThat(entity.getDeliveryClaimedAt()).isEqualTo(now);
    }

    @Test
    void shouldReadAndWriteSubscriptionEntityFields() {
        SubscriptionEntity entity = new SubscriptionEntity();
        Instant now = Instant.parse("2024-03-15T09:30:22Z");
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        entity.setId(id);
        entity.setClientId("CLIENT001");
        entity.setEventType("credit_card_payment");
        entity.setTargetUrl("https://client001.example.com/webhooks/credit-card-payment");
        entity.setActive(true);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getClientId()).isEqualTo("CLIENT001");
        assertThat(entity.getEventType()).isEqualTo("credit_card_payment");
        assertThat(entity.getTargetUrl()).contains("https://");
        assertThat(entity.isActive()).isTrue();
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldExposeDtoFieldsAndDeliveryResultVariants() {
        Instant now = Instant.parse("2024-03-15T09:30:22Z");
        NotificationEventSummaryResponse summary = new NotificationEventSummaryResponse(
                "EVT001",
                "EVT001",
                "credit_card_payment",
                "Credit card payment received for $150.00",
                now,
                "FAILED",
                null,
                now);
        NotificationEventDetailResponse detail = new NotificationEventDetailResponse(
                "EVT001",
                "EVT001",
                "credit_card_payment",
                "Credit card payment received for $150.00",
                now,
                "FAILED",
                null,
                now,
                4,
                503,
                "Webhook endpoint unavailable");
        NotificationEventsResponse collection = new NotificationEventsResponse(List.of(summary));
        ReplayNotificationResponse replay = new ReplayNotificationResponse("EVT001", "PENDING", "scheduled");
        DeliveryResult success = DeliveryResult.success(200);
        DeliveryResult retryable = DeliveryResult.retryableFailure(503, "retry");
        DeliveryResult permanent = DeliveryResult.permanentFailure(400, "rejected");

        assertThat(summary.notificationEventId()).isEqualTo("EVT001");
        assertThat(detail.failureReason()).isEqualTo("Webhook endpoint unavailable");
        assertThat(collection.items()).containsExactly(summary);
        assertThat(replay.message()).isEqualTo("scheduled");
        assertThat(success.success()).isTrue();
        assertThat(retryable.retryable()).isTrue();
        assertThat(permanent.errorMessage()).isEqualTo("rejected");
    }

    @Test
    void shouldValidateValueObjectsAndExceptionMessages() {
        assertThat(new ClientId("CLIENT001").value()).isEqualTo("CLIENT001");
        assertThat(new EventType("credit_card_payment").value()).isEqualTo("credit_card_payment");
        assertThat(new NotificationEventId("EVT001").value()).isEqualTo("EVT001");
        assertThat(new SourceEventId("EVT001").value()).isEqualTo("EVT001");
        assertThat(new NotificationEventNotFoundException("EVT001"))
                .hasMessage("Notification event not found: EVT001");
        assertThat(new ReplayNotAllowedException("EVT002"))
                .hasMessage("Replay not allowed for notification event: EVT002");

        assertThatThrownBy(() -> new ClientId(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("clientId cannot be blank");
        assertThatThrownBy(() -> new EventType(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("eventType cannot be blank");
        assertThatThrownBy(() -> new NotificationEventId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("notificationEventId cannot be blank");
        assertThatThrownBy(() -> new SourceEventId("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sourceEventId cannot be blank");
    }
}
