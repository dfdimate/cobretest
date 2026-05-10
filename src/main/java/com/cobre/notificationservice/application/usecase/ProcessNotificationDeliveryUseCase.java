package com.cobre.notificationservice.application.usecase;

import com.cobre.notificationservice.application.port.out.ClockPort;
import com.cobre.notificationservice.application.port.out.DeliveryResult;
import com.cobre.notificationservice.application.port.out.NotificationEventRepositoryPort;
import com.cobre.notificationservice.application.port.out.ObservabilityPort;
import com.cobre.notificationservice.application.port.out.SubscriptionRepositoryPort;
import com.cobre.notificationservice.application.port.out.WebhookDeliveryPort;
import com.cobre.notificationservice.domain.model.NotificationEvent;
import com.cobre.notificationservice.domain.model.Subscription;
import com.cobre.notificationservice.domain.model.value.NotificationEventId;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class ProcessNotificationDeliveryUseCase {

    private final NotificationEventRepositoryPort notificationEventRepository;
    private final SubscriptionRepositoryPort subscriptionRepository;
    private final WebhookDeliveryPort webhookDeliveryPort;
    private final ObservabilityPort observabilityPort;
    private final ClockPort clockPort;
    private final int maxAttempts = 4;
    private final long initialBackoffSeconds = 30L;

    public ProcessNotificationDeliveryUseCase(
            NotificationEventRepositoryPort notificationEventRepository,
            SubscriptionRepositoryPort subscriptionRepository,
            WebhookDeliveryPort webhookDeliveryPort,
            ObservabilityPort observabilityPort,
            ClockPort clockPort) {
        this.notificationEventRepository = notificationEventRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.webhookDeliveryPort = webhookDeliveryPort;
        this.observabilityPort = observabilityPort;
        this.clockPort = clockPort;
    }

    public void process(NotificationEventId notificationEventId) {
        notificationEventRepository.findById(notificationEventId)
                .filter(NotificationEvent::isReadyForDelivery)
                .ifPresent(this::deliver);
    }

    private void deliver(NotificationEvent notificationEvent) {
        Subscription subscription = subscriptionRepository
                .findActiveByClientIdAndEventType(notificationEvent.clientId(), notificationEvent.eventType())
                .orElse(null);

        if (subscription == null) {
            notificationEvent.markFailed(clockPort.now(), null, "No active subscription");
            notificationEventRepository.save(notificationEvent);
            observabilityPort.recordFailed(notificationEvent);
            return;
        }

        Instant startedAt = clockPort.now();
        notificationEvent.markDelivering(startedAt);
        notificationEventRepository.save(notificationEvent);

        DeliveryResult result = webhookDeliveryPort.deliver(notificationEvent, subscription);
        Instant finishedAt = clockPort.now();

        if (result.success()) {
            notificationEvent.markCompleted(finishedAt, result.httpStatus());
            observabilityPort.recordCompleted(notificationEvent);
        } else if (result.retryable() && notificationEvent.hasRetriesRemaining(maxAttempts)) {
            notificationEvent.markRetryableFailure(
                    finishedAt,
                    result.httpStatus(),
                    result.errorMessage(),
                    calculateNextRetryAt(finishedAt, notificationEvent.attemptCount()));
            observabilityPort.recordRetryableFailure(notificationEvent);
        } else {
            notificationEvent.markFailed(finishedAt, result.httpStatus(), result.errorMessage());
            observabilityPort.recordFailed(notificationEvent);
        }

        notificationEventRepository.save(notificationEvent);
    }

    private Instant calculateNextRetryAt(Instant attemptedAt, int attemptCount) {
        long multiplier = 1L << Math.max(0, attemptCount - 1);
        return attemptedAt.plusSeconds(initialBackoffSeconds * multiplier);
    }
}

