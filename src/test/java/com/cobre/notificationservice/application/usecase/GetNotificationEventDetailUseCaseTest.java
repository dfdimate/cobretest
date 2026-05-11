package com.cobre.notificationservice.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cobre.notificationservice.application.port.out.NotificationEventRepositoryPort;
import com.cobre.notificationservice.domain.model.NotificationEvent;
import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.EventType;
import com.cobre.notificationservice.domain.model.value.NotificationEventId;
import com.cobre.notificationservice.domain.model.value.SourceEventId;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GetNotificationEventDetailUseCaseTest {

    @Test
    void shouldReturnNotificationForOwningClient() {
        NotificationEventRepositoryPort repository = mock(NotificationEventRepositoryPort.class);
        GetNotificationEventDetailUseCase useCase = new GetNotificationEventDetailUseCase(repository);
        NotificationEvent event = NotificationEvent.pending(
                new NotificationEventId("EVT001"),
                new SourceEventId("EVT001"),
                new ClientId("CLIENT001"),
                new EventType("credit_card_payment"),
                "Credit card payment received for $150.00",
                Instant.parse("2024-03-15T09:30:22Z"),
                Instant.parse("2024-03-15T09:30:23Z"));
        when(repository.findById(new NotificationEventId("EVT001"))).thenReturn(Optional.of(event));

        NotificationEvent result = useCase.get(new NotificationEventId("EVT001"), new ClientId("CLIENT001"));

        assertThat(result).isSameAs(event);
    }

    @Test
    void shouldHideNotificationFromDifferentClient() {
        NotificationEventRepositoryPort repository = mock(NotificationEventRepositoryPort.class);
        GetNotificationEventDetailUseCase useCase = new GetNotificationEventDetailUseCase(repository);
        NotificationEvent event = NotificationEvent.pending(
                new NotificationEventId("EVT001"),
                new SourceEventId("EVT001"),
                new ClientId("CLIENT001"),
                new EventType("credit_card_payment"),
                "Credit card payment received for $150.00",
                Instant.parse("2024-03-15T09:30:22Z"),
                Instant.parse("2024-03-15T09:30:23Z"));
        when(repository.findById(new NotificationEventId("EVT001"))).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> useCase.get(new NotificationEventId("EVT001"), new ClientId("CLIENT999")))
                .isInstanceOf(NotificationEventNotFoundException.class)
                .hasMessage("Notification event not found: EVT001");
    }
}
