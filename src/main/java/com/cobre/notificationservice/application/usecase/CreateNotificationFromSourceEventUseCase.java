package com.cobre.notificationservice.application.usecase;

import com.cobre.notificationservice.application.port.out.ClockPort;
import com.cobre.notificationservice.application.port.out.NotificationEventRepositoryPort;
import com.cobre.notificationservice.application.port.out.SubscriptionRepositoryPort;
import com.cobre.notificationservice.domain.model.NotificationEvent;
import com.cobre.notificationservice.domain.model.SourceEvent;
import com.cobre.notificationservice.domain.model.value.NotificationEventId;
import org.springframework.stereotype.Service;

@Service
public class CreateNotificationFromSourceEventUseCase {

    private final SubscriptionRepositoryPort subscriptionRepository;
    private final NotificationEventRepositoryPort notificationEventRepository;
    private final ClockPort clockPort;

    public CreateNotificationFromSourceEventUseCase(
            SubscriptionRepositoryPort subscriptionRepository,
            NotificationEventRepositoryPort notificationEventRepository,
            ClockPort clockPort) {
        this.subscriptionRepository = subscriptionRepository;
        this.notificationEventRepository = notificationEventRepository;
        this.clockPort = clockPort;
    }

    public void handle(SourceEvent sourceEvent) {
        NotificationEventId notificationEventId = new NotificationEventId(sourceEvent.sourceEventId().value());

        if (notificationEventRepository.findById(notificationEventId).isPresent()) {
            return;
        }

        subscriptionRepository.findActiveByClientIdAndEventType(sourceEvent.clientId(), sourceEvent.eventType())
                .ifPresent(subscription -> notificationEventRepository.save(NotificationEvent.pending(
                        notificationEventId,
                        sourceEvent.sourceEventId(),
                        sourceEvent.clientId(),
                        sourceEvent.eventType(),
                        sourceEvent.content(),
                        sourceEvent.eventCreatedAt(),
                        clockPort.now())));
    }
}

