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
}

