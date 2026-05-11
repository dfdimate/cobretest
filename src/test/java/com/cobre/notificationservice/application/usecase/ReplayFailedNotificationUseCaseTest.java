package com.cobre.notificationservice.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cobre.notificationservice.application.port.out.ClockPort;
import com.cobre.notificationservice.application.port.out.NotificationEventRepositoryPort;
import com.cobre.notificationservice.domain.model.NotificationEvent;
import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.EventType;
import com.cobre.notificationservice.domain.model.value.NotificationEventId;
import com.cobre.notificationservice.domain.model.value.SourceEventId;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReplayFailedNotificationUseCaseTest {

    @Mock
    private NotificationEventRepositoryPort notificationEventRepository;

    @Mock
    private ClockPort clockPort;

    @InjectMocks
    private ReplayFailedNotificationUseCase useCase;

    @Test
    void shouldRejectReplayWhenNotificationIsNotFailed() {
        NotificationEvent notificationEvent = NotificationEvent.pending(
                new NotificationEventId("EVT001"),
                new SourceEventId("EVT001"),
                new ClientId("CLIENT001"),
                new EventType("credit_card_payment"),
                "Credit card payment received for $150.00",
                Instant.parse("2024-03-15T09:30:22Z"),
                Instant.parse("2024-03-15T09:30:23Z"));

        when(notificationEventRepository.findById(new NotificationEventId("EVT001")))
                .thenReturn(Optional.of(notificationEvent));

        assertThatThrownBy(() -> useCase.replay(new NotificationEventId("EVT001"), new ClientId("CLIENT001")))
                .isInstanceOf(ReplayNotAllowedException.class);
    }

    @Test
    void shouldRequeueFailedNotification() {
        NotificationEvent notificationEvent = NotificationEvent.failed(
                new NotificationEventId("EVT003"),
                new SourceEventId("EVT003"),
                new ClientId("CLIENT002"),
                new EventType("credit_transfer"),
                "Bank transfer received from Account #4567 for $1,500.00",
                Instant.parse("2024-03-15T11:20:18Z"),
                Instant.parse("2024-03-15T11:20:19Z"),
                Instant.parse("2024-03-15T11:45:00Z"),
                503,
                "Webhook endpoint unavailable",
                4);

        when(notificationEventRepository.findById(new NotificationEventId("EVT003")))
                .thenReturn(Optional.of(notificationEvent));
        when(clockPort.now()).thenReturn(Instant.parse("2024-03-15T12:00:00Z"));

        useCase.replay(new NotificationEventId("EVT003"), new ClientId("CLIENT002"));

        verify(notificationEventRepository).save(argThat(event ->
                event.deliveryStatus().name().equals("PENDING")
                        && event.notificationEventId().value().equals("EVT003")));
    }
}
