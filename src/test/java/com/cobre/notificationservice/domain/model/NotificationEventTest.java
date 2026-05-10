package com.cobre.notificationservice.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.DeliveryStatus;
import com.cobre.notificationservice.domain.model.value.EventType;
import com.cobre.notificationservice.domain.model.value.NotificationEventId;
import com.cobre.notificationservice.domain.model.value.SourceEventId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class NotificationEventTest {

    @Test
    void shouldMarkNotificationAsCompletedWhenWebhookSucceeds() {
        NotificationEvent event = NotificationEvent.pending(
                new NotificationEventId("EVT001"),
                new SourceEventId("EVT001"),
                new ClientId("CLIENT001"),
                new EventType("credit_card_payment"),
                "Credit card payment received for $150.00",
                Instant.parse("2024-03-15T09:30:22Z"),
                Instant.parse("2024-03-15T09:30:23Z"));

        event.markDelivering(Instant.parse("2024-03-15T10:00:00Z"));
        event.markCompleted(Instant.parse("2024-03-15T10:00:02Z"), 200);

        assertThat(event.deliveryStatus()).isEqualTo(DeliveryStatus.COMPLETED);
        assertThat(event.deliveredAt()).isEqualTo(Instant.parse("2024-03-15T10:00:02Z"));
        assertThat(event.httpStatus()).isEqualTo(200);
    }

    @Test
    void shouldAllowReplayOnlyWhenFailed() {
        NotificationEvent event = NotificationEvent.failed(
                new NotificationEventId("EVT003"),
                new SourceEventId("EVT003"),
                new ClientId("CLIENT002"),
                new EventType("credit_transfer"),
                "Bank transfer received from Account #4567 for $1,500.00",
                Instant.parse("2024-03-15T11:20:18Z"),
                Instant.parse("2024-03-15T11:20:20Z"),
                Instant.parse("2024-03-15T11:45:00Z"),
                503,
                "Webhook endpoint unavailable",
                4);

        assertThat(event.canReplay()).isTrue();
    }
}
