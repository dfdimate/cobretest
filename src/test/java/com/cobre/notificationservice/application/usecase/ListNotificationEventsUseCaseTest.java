package com.cobre.notificationservice.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cobre.notificationservice.application.port.out.NotificationEventRepositoryPort;
import com.cobre.notificationservice.domain.model.NotificationEvent;
import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.DeliveryStatus;
import com.cobre.notificationservice.domain.model.value.EventType;
import com.cobre.notificationservice.domain.model.value.NotificationEventId;
import com.cobre.notificationservice.domain.model.value.SourceEventId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ListNotificationEventsUseCaseTest {

    @Test
    void shouldDelegateListingWithFilters() {
        NotificationEventRepositoryPort repository = mock(NotificationEventRepositoryPort.class);
        ListNotificationEventsUseCase useCase = new ListNotificationEventsUseCase(repository);
        NotificationEvent event = NotificationEvent.pending(
                new NotificationEventId("EVT001"),
                new SourceEventId("EVT001"),
                new ClientId("CLIENT001"),
                new EventType("credit_card_payment"),
                "Credit card payment received for $150.00",
                Instant.parse("2024-03-15T09:30:22Z"),
                Instant.parse("2024-03-15T09:30:23Z"));
        when(repository.findByClientAndFilters(
                new ClientId("CLIENT001"),
                Instant.parse("2024-03-15T00:00:00Z"),
                Instant.parse("2024-03-16T00:00:00Z"),
                DeliveryStatus.FAILED)).thenReturn(List.of(event));

        var result = useCase.list(
                new ClientId("CLIENT001"),
                Instant.parse("2024-03-15T00:00:00Z"),
                Instant.parse("2024-03-16T00:00:00Z"),
                DeliveryStatus.FAILED);

        assertThat(result).containsExactly(event);
        verify(repository).findByClientAndFilters(
                new ClientId("CLIENT001"),
                Instant.parse("2024-03-15T00:00:00Z"),
                Instant.parse("2024-03-16T00:00:00Z"),
                DeliveryStatus.FAILED);
    }
}
