package com.cobre.notificationservice.infrastructure.observability;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.cobre.notificationservice.domain.model.NotificationEvent;
import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.EventType;
import com.cobre.notificationservice.domain.model.value.NotificationEventId;
import com.cobre.notificationservice.domain.model.value.SourceEventId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class LoggingObservabilityAdapterTest {

    private final LoggingObservabilityAdapter adapter = new LoggingObservabilityAdapter();

    @Test
    void shouldLogCompletedNotification() {
        NotificationEvent event = NotificationEvent.pending(
                new NotificationEventId("EVT001"),
                new SourceEventId("EVT001"),
                new ClientId("CLIENT001"),
                new EventType("credit_card_payment"),
                "Credit card payment received for $150.00",
                Instant.parse("2024-03-15T09:30:22Z"),
                Instant.parse("2024-03-15T09:30:23Z"));
        event.markDelivering(Instant.parse("2024-03-15T10:00:00Z"));
        event.markCompleted(Instant.parse("2024-03-15T10:00:01Z"), 200);

        assertThatCode(() -> adapter.recordCompleted(event)).doesNotThrowAnyException();
    }

    @Test
    void shouldLogRetryableFailureNotification() {
        NotificationEvent event = NotificationEvent.pending(
                new NotificationEventId("EVT002"),
                new SourceEventId("EVT002"),
                new ClientId("CLIENT001"),
                new EventType("debit_card_withdrawal"),
                "ATM withdrawal of $200.00",
                Instant.parse("2024-03-15T10:15:45Z"),
                Instant.parse("2024-03-15T10:15:46Z"));
        event.markDelivering(Instant.parse("2024-03-15T10:20:00Z"));
        event.markRetryableFailure(
                Instant.parse("2024-03-15T10:20:01Z"),
                503,
                "endpoint unavailable",
                Instant.parse("2024-03-15T10:21:01Z"));

        assertThatCode(() -> adapter.recordRetryableFailure(event)).doesNotThrowAnyException();
    }

    @Test
    void shouldLogFailedNotification() {
        NotificationEvent event = NotificationEvent.failed(
                new NotificationEventId("EVT003"),
                new SourceEventId("EVT003"),
                new ClientId("CLIENT002"),
                new EventType("credit_transfer"),
                "Bank transfer received from Account #4567 for $1,500.00",
                Instant.parse("2024-03-15T11:20:18Z"),
                Instant.parse("2024-03-15T11:20:19Z"),
                Instant.parse("2024-03-15T11:25:00Z"),
                503,
                "Webhook endpoint unavailable",
                4);

        assertThatCode(() -> adapter.recordFailed(event)).doesNotThrowAnyException();
    }
}
