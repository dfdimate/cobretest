package com.cobre.notificationservice.application.usecase;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cobre.notificationservice.application.port.out.ClockPort;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RetryDueNotificationsUseCaseTest {

    @Mock
    private NotificationEventRepositoryPort notificationEventRepository;

    @Mock
    private ProcessNotificationDeliveryUseCase processNotificationDeliveryUseCase;

    @Mock
    private ClockPort clockPort;

    @InjectMocks
    private RetryDueNotificationsUseCase useCase;

    @Test
    void shouldProcessDueNotificationIdsAfterRecoveringStaleClaims() {
        Instant now = Instant.parse("2024-03-15T10:00:00Z");
        when(clockPort.now()).thenReturn(now);
        when(notificationEventRepository.findStaleDeliveries(Instant.parse("2024-03-15T09:55:00Z")))
                .thenReturn(List.of());
        when(notificationEventRepository.findDueForDelivery(now))
                .thenReturn(List.of(new NotificationEventId("EVT001"), new NotificationEventId("EVT002")));

        useCase.setStaleClaimTimeoutSeconds(300L);
        useCase.processDueNotifications();

        verify(processNotificationDeliveryUseCase).process(new NotificationEventId("EVT001"));
        verify(processNotificationDeliveryUseCase).process(new NotificationEventId("EVT002"));
    }

    @Test
    void shouldReturnStaleDeliveryToRetryableWhenAttemptsRemain() {
        Instant now = Instant.parse("2024-03-15T10:00:00Z");
        NotificationEvent staleDelivery = NotificationEvent.restore(
                new NotificationEventId("EVT003"),
                new SourceEventId("EVT003"),
                new ClientId("CLIENT002"),
                new EventType("credit_transfer"),
                "Bank transfer received from Account #4567 for $1,500.00",
                Instant.parse("2024-03-15T09:00:00Z"),
                Instant.parse("2024-03-15T09:00:01Z"),
                DeliveryStatus.DELIVERING,
                1,
                Instant.parse("2024-03-15T09:50:00Z"),
                null,
                null,
                null,
                null);

        when(clockPort.now()).thenReturn(now);
        when(notificationEventRepository.findStaleDeliveries(Instant.parse("2024-03-15T09:55:00Z")))
                .thenReturn(List.of(staleDelivery));
        when(notificationEventRepository.findDueForDelivery(now)).thenReturn(List.of());

        useCase.setMaxAttempts(4);
        useCase.setStaleClaimTimeoutSeconds(300L);
        useCase.processDueNotifications();

        verify(notificationEventRepository).save(argThat(event ->
                event.notificationEventId().value().equals("EVT003")
                        && event.deliveryStatus() == DeliveryStatus.FAILED_RETRYABLE
                        && event.nextRetryAt().equals(now)));
    }

    @Test
    void shouldMarkStaleDeliveryFailedWhenAttemptsAreExhausted() {
        Instant now = Instant.parse("2024-03-15T10:00:00Z");
        NotificationEvent staleDelivery = NotificationEvent.restore(
                new NotificationEventId("EVT004"),
                new SourceEventId("EVT004"),
                new ClientId("CLIENT002"),
                new EventType("debit_automatic_payment"),
                "Monthly utility bill payment of $85.50",
                Instant.parse("2024-03-15T09:00:00Z"),
                Instant.parse("2024-03-15T09:00:01Z"),
                DeliveryStatus.DELIVERING,
                4,
                Instant.parse("2024-03-15T09:50:00Z"),
                null,
                null,
                null,
                null);

        when(clockPort.now()).thenReturn(now);
        when(notificationEventRepository.findStaleDeliveries(Instant.parse("2024-03-15T09:55:00Z")))
                .thenReturn(List.of(staleDelivery));
        when(notificationEventRepository.findDueForDelivery(now)).thenReturn(List.of());

        useCase.setMaxAttempts(4);
        useCase.setStaleClaimTimeoutSeconds(300L);
        useCase.processDueNotifications();

        verify(notificationEventRepository).save(argThat(event ->
                event.notificationEventId().value().equals("EVT004")
                        && event.deliveryStatus() == DeliveryStatus.FAILED
                        && "Delivery claim timed out before completion".equals(event.finalFailureReason())));
    }
}
