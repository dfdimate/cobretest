package com.cobre.notificationservice.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.cobre.notificationservice.application.port.out.NotificationEventRepositoryPort;
import com.cobre.notificationservice.domain.model.NotificationEvent;
import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.DeliveryStatus;
import com.cobre.notificationservice.domain.model.value.EventType;
import com.cobre.notificationservice.domain.model.value.NotificationEventId;
import com.cobre.notificationservice.domain.model.value.SourceEventId;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class NotificationEventRepositoryAdapterIT {

    @Autowired
    private NotificationEventRepositoryPort repository;

    @Test
    void shouldPersistAndReadNotificationEventByClientAndStatus() {
        NotificationEvent event = NotificationEvent.pending(
                new NotificationEventId("EVT999"),
                new SourceEventId("EVT999"),
                new ClientId("CLIENT001"),
                new EventType("credit_card_payment"),
                "Credit card payment received for $999.00",
                Instant.parse("2024-03-15T19:00:00Z"),
                Instant.parse("2024-03-15T19:00:01Z"));

        repository.save(event);

        var results = repository.findByClientAndFilters(
                new ClientId("CLIENT001"),
                null,
                null,
                DeliveryStatus.PENDING);

        assertThat(results)
                .extracting(saved -> saved.notificationEventId().value())
                .contains("EVT999");
    }

    @Test
    void shouldReadNotificationEventsWithoutOptionalFilters() {
        NotificationEvent event = NotificationEvent.pending(
                new NotificationEventId("EVT998"),
                new SourceEventId("EVT998"),
                new ClientId("CLIENT001"),
                new EventType("credit_refund"),
                "Refund processed for order #998 for $45.00",
                Instant.parse("2024-03-15T20:00:00Z"),
                Instant.parse("2024-03-15T20:00:01Z"));

        repository.save(event);

        var results = repository.findByClientAndFilters(
                new ClientId("CLIENT001"),
                null,
                null,
                null);

        assertThat(results)
                .extracting(saved -> saved.notificationEventId().value())
                .contains("EVT998");
    }

    @Test
    void shouldClaimDueNotificationForDeliveryOnlyOnce() {
        NotificationEvent event = NotificationEvent.pending(
                new NotificationEventId("EVT997"),
                new SourceEventId("EVT997"),
                new ClientId("CLIENT001"),
                new EventType("credit_card_payment"),
                "Credit card payment received for $997.00",
                Instant.parse("2024-03-15T21:00:00Z"),
                Instant.parse("2024-03-15T21:00:01Z"));

        repository.save(event);

        var claimed = repository.claimForDelivery(
                new NotificationEventId("EVT997"),
                Instant.parse("2024-03-15T21:05:00Z"),
                Instant.parse("2024-03-15T21:05:00Z"));
        var claimedAgain = repository.claimForDelivery(
                new NotificationEventId("EVT997"),
                Instant.parse("2024-03-15T21:06:00Z"),
                Instant.parse("2024-03-15T21:06:00Z"));

        assertThat(claimed).isPresent();
        assertThat(claimed.orElseThrow().deliveryStatus()).isEqualTo(DeliveryStatus.DELIVERING);
        assertThat(claimed.orElseThrow().attemptCount()).isEqualTo(1);
        assertThat(claimedAgain).isEmpty();
    }

    @Test
    void shouldOnlyRequeueFailedNotifications() {
        NotificationEvent failedEvent = NotificationEvent.failed(
                new NotificationEventId("EVT996"),
                new SourceEventId("EVT996"),
                new ClientId("CLIENT003"),
                new EventType("credit_refund"),
                "Refund processed for order #996 for $30.00",
                Instant.parse("2024-03-15T22:00:00Z"),
                Instant.parse("2024-03-15T22:00:01Z"),
                Instant.parse("2024-03-15T22:10:00Z"),
                503,
                "Webhook endpoint unavailable",
                4);
        NotificationEvent pendingEvent = NotificationEvent.pending(
                new NotificationEventId("EVT995"),
                new SourceEventId("EVT995"),
                new ClientId("CLIENT003"),
                new EventType("credit_refund"),
                "Refund processed for order #995 for $31.00",
                Instant.parse("2024-03-15T22:30:00Z"),
                Instant.parse("2024-03-15T22:30:01Z"));

        repository.save(failedEvent);
        repository.save(pendingEvent);

        var requeuedFailed = repository.requeueIfFailed(
                new NotificationEventId("EVT996"),
                Instant.parse("2024-03-15T22:15:00Z"));
        var requeuedPending = repository.requeueIfFailed(
                new NotificationEventId("EVT995"),
                Instant.parse("2024-03-15T22:35:00Z"));

        assertThat(requeuedFailed).isPresent();
        assertThat(requeuedFailed.orElseThrow().deliveryStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(requeuedPending).isEmpty();
    }

    @Test
    void shouldFindStaleDeliveriesByClaimTimestamp() {
        NotificationEvent event = NotificationEvent.pending(
                new NotificationEventId("EVT994"),
                new SourceEventId("EVT994"),
                new ClientId("CLIENT002"),
                new EventType("debit_automatic_payment"),
                "Monthly utility bill payment of $84.00",
                Instant.parse("2024-03-15T23:00:00Z"),
                Instant.parse("2024-03-15T23:00:01Z"));
        event.markDelivering(Instant.parse("2024-03-15T23:05:00Z"));

        repository.save(event);

        var staleDeliveries = repository.findStaleDeliveries(Instant.parse("2024-03-15T23:06:00Z"));

        assertThat(staleDeliveries)
                .extracting(saved -> saved.notificationEventId().value())
                .contains("EVT994");
    }
}
