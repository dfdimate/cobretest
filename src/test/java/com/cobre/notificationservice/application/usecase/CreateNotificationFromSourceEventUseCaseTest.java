package com.cobre.notificationservice.application.usecase;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cobre.notificationservice.application.port.out.ClockPort;
import com.cobre.notificationservice.application.port.out.NotificationEventRepositoryPort;
import com.cobre.notificationservice.application.port.out.SubscriptionRepositoryPort;
import com.cobre.notificationservice.domain.model.SourceEvent;
import com.cobre.notificationservice.domain.model.Subscription;
import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.DeliveryStatus;
import com.cobre.notificationservice.domain.model.value.EventType;
import com.cobre.notificationservice.domain.model.value.SourceEventId;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateNotificationFromSourceEventUseCaseTest {

    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;

    @Mock
    private NotificationEventRepositoryPort notificationEventRepository;

    @Mock
    private ClockPort clockPort;

    @InjectMocks
    private CreateNotificationFromSourceEventUseCase useCase;

    @Test
    void shouldCreatePendingNotificationWhenSubscriptionExists() {
        SourceEvent sourceEvent = new SourceEvent(
                new SourceEventId("EVT001"),
                new ClientId("CLIENT001"),
                new EventType("credit_card_payment"),
                "Credit card payment received for $150.00",
                Instant.parse("2024-03-15T09:30:22Z"));

        Subscription subscription = new Subscription(
                new ClientId("CLIENT001"),
                new EventType("credit_card_payment"),
                "https://client001.example.com/webhooks/credit-card-payment",
                true);

        when(subscriptionRepository.findActiveByClientIdAndEventType(
                new ClientId("CLIENT001"),
                new EventType("credit_card_payment"))).thenReturn(Optional.of(subscription));
        when(clockPort.now()).thenReturn(Instant.parse("2024-03-15T09:30:23Z"));

        useCase.handle(sourceEvent);

        verify(notificationEventRepository).save(argThat(event ->
                event.deliveryStatus() == DeliveryStatus.PENDING
                        && event.notificationEventId().value().equals("EVT001")));
    }
}
