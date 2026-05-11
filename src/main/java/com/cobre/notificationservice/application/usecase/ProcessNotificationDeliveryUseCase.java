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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProcessNotificationDeliveryUseCase {

    private final NotificationEventRepositoryPort notificationEventRepository;
    private final SubscriptionRepositoryPort subscriptionRepository;
    private final WebhookDeliveryPort webhookDeliveryPort;
    private final ObservabilityPort observabilityPort;
    private final ClockPort clockPort;
    private int maxAttempts = 4;
    private long initialBackoffSeconds = 30L;

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

    @Value("${notifications.delivery.max-attempts:4}")
    void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Value("${notifications.delivery.initial-backoff-seconds:30}")
    void setInitialBackoffSeconds(long initialBackoffSeconds) {
        this.initialBackoffSeconds = initialBackoffSeconds;
    }

    public void process(NotificationEventId notificationEventId) {
        Instant claimedAt = clockPort.now();
        notificationEventRepository.claimForDelivery(notificationEventId, claimedAt, claimedAt)
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
