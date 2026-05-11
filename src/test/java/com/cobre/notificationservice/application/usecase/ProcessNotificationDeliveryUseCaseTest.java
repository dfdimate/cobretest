package com.cobre.notificationservice.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cobre.notificationservice.application.port.out.ClockPort;
import com.cobre.notificationservice.application.port.out.DeliveryResult;
import com.cobre.notificationservice.application.port.out.NotificationEventRepositoryPort;
import com.cobre.notificationservice.application.port.out.ObservabilityPort;
import com.cobre.notificationservice.application.port.out.SubscriptionRepositoryPort;
import com.cobre.notificationservice.application.port.out.WebhookDeliveryPort;
import com.cobre.notificationservice.domain.model.NotificationEvent;
import com.cobre.notificationservice.domain.model.Subscription;
import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.DeliveryStatus;
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
class ProcessNotificationDeliveryUseCaseTest {

    @Mock
    private NotificationEventRepositoryPort notificationEventRepository;

    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;

    @Mock
    private WebhookDeliveryPort webhookDeliveryPort;

    @Mock
    private ObservabilityPort observabilityPort;

    @Mock
    private ClockPort clockPort;

    @InjectMocks
    private ProcessNotificationDeliveryUseCase useCase;

    @Test
    void shouldCompleteNotificationOnAny2xxResponse() {
        NotificationEvent claimedNotificationEvent = NotificationEvent.pending(
                new NotificationEventId("EVT001"),
                new SourceEventId("EVT001"),
                new ClientId("CLIENT001"),
                new EventType("credit_card_payment"),
                "Credit card payment received for $150.00",
                Instant.parse("2024-03-15T09:30:22Z"),
                Instant.parse("2024-03-15T09:30:23Z"));
        claimedNotificationEvent.markDelivering(Instant.parse("2024-03-15T10:00:00Z"));
        Subscription subscription = new Subscription(
                new ClientId("CLIENT001"),
                new EventType("credit_card_payment"),
                "https://client001.example.com/webhooks/credit-card-payment",
                true);

        when(notificationEventRepository.claimForDelivery(
                new NotificationEventId("EVT001"),
                Instant.parse("2024-03-15T10:00:00Z"),
                Instant.parse("2024-03-15T10:00:00Z")))
                .thenReturn(Optional.of(claimedNotificationEvent));
        when(subscriptionRepository.findActiveByClientIdAndEventType(
                new ClientId("CLIENT001"),
                new EventType("credit_card_payment"))).thenReturn(Optional.of(subscription));
        when(clockPort.now())
                .thenReturn(
                        Instant.parse("2024-03-15T10:00:00Z"),
                        Instant.parse("2024-03-15T10:00:02Z"));
        when(webhookDeliveryPort.deliver(claimedNotificationEvent, subscription))
                .thenReturn(DeliveryResult.success(202));

        useCase.process(new NotificationEventId("EVT001"));

        verify(notificationEventRepository, atLeastOnce()).save(argThat(event ->
                event.deliveryStatus() == DeliveryStatus.COMPLETED
                        && event.httpStatus() == 202));
    }

    @Test
    void shouldMarkRetryableFailureAndScheduleNextRetry() {
        NotificationEvent claimedNotificationEvent = NotificationEvent.pending(
                new NotificationEventId("EVT003"),
                new SourceEventId("EVT003"),
                new ClientId("CLIENT002"),
                new EventType("credit_transfer"),
                "Bank transfer received from Account #4567 for $1,500.00",
                Instant.parse("2024-03-15T11:20:18Z"),
                Instant.parse("2024-03-15T11:20:19Z"));
        claimedNotificationEvent.markDelivering(Instant.parse("2024-03-15T11:25:00Z"));
        Subscription subscription = new Subscription(
                new ClientId("CLIENT002"),
                new EventType("credit_transfer"),
                "https://client002.example.com/webhooks/credit-transfer",
                true);

        when(notificationEventRepository.claimForDelivery(
                new NotificationEventId("EVT003"),
                Instant.parse("2024-03-15T11:25:00Z"),
                Instant.parse("2024-03-15T11:25:00Z")))
                .thenReturn(Optional.of(claimedNotificationEvent));
        when(subscriptionRepository.findActiveByClientIdAndEventType(
                new ClientId("CLIENT002"),
                new EventType("credit_transfer"))).thenReturn(Optional.of(subscription));
        when(clockPort.now())
                .thenReturn(
                        Instant.parse("2024-03-15T11:25:00Z"),
                        Instant.parse("2024-03-15T11:25:01Z"));
        when(webhookDeliveryPort.deliver(claimedNotificationEvent, subscription))
                .thenReturn(DeliveryResult.retryableFailure(503, "endpoint unavailable"));

        useCase.process(new NotificationEventId("EVT003"));

        verify(notificationEventRepository, atLeastOnce()).save(argThat(event ->
                event.deliveryStatus() == DeliveryStatus.FAILED_RETRYABLE
                        && event.httpStatus() == 503
                        && event.nextRetryAt() != null));
    }

    @Test
    void shouldSkipDeliveryWhenAtomicClaimFails() {
        when(clockPort.now()).thenReturn(Instant.parse("2024-03-15T11:25:00Z"));
        when(notificationEventRepository.claimForDelivery(any(), any(), any())).thenReturn(Optional.empty());

        useCase.process(new NotificationEventId("EVT404"));

        verify(webhookDeliveryPort, org.mockito.Mockito.never()).deliver(any(), any());
        verify(notificationEventRepository, org.mockito.Mockito.never()).save(any());
    }
}
