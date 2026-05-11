package com.cobre.notificationservice.application.usecase;

import com.cobre.notificationservice.application.port.out.ClockPort;
import com.cobre.notificationservice.application.port.out.NotificationEventRepositoryPort;
import com.cobre.notificationservice.domain.model.NotificationEvent;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RetryDueNotificationsUseCase {

    private final NotificationEventRepositoryPort notificationEventRepository;
    private final ProcessNotificationDeliveryUseCase processNotificationDeliveryUseCase;
    private final ClockPort clockPort;
    private int maxAttempts = 4;
    private long staleClaimTimeoutSeconds = 300L;

    public RetryDueNotificationsUseCase(
            NotificationEventRepositoryPort notificationEventRepository,
            ProcessNotificationDeliveryUseCase processNotificationDeliveryUseCase,
            ClockPort clockPort) {
        this.notificationEventRepository = notificationEventRepository;
        this.processNotificationDeliveryUseCase = processNotificationDeliveryUseCase;
        this.clockPort = clockPort;
    }

    @Value("${notifications.delivery.max-attempts:4}")
    void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Value("${notifications.delivery.stale-claim-timeout-seconds:300}")
    void setStaleClaimTimeoutSeconds(long staleClaimTimeoutSeconds) {
        this.staleClaimTimeoutSeconds = staleClaimTimeoutSeconds;
    }

    public void processDueNotifications() {
        Instant now = clockPort.now();
        recoverStaleDeliveries(now);
        notificationEventRepository.findDueForDelivery(now).forEach(processNotificationDeliveryUseCase::process);
    }

    private void recoverStaleDeliveries(Instant now) {
        Instant claimedBefore = now.minusSeconds(staleClaimTimeoutSeconds);

        for (NotificationEvent notificationEvent : notificationEventRepository.findStaleDeliveries(claimedBefore)) {
            if (notificationEvent.hasRetriesRemaining(maxAttempts)) {
                notificationEvent.markRetryableFailure(
                        now,
                        notificationEvent.httpStatus(),
                        "Delivery claim timed out before completion",
                        now);
            } else {
                notificationEvent.markFailed(
                        now,
                        notificationEvent.httpStatus(),
                        "Delivery claim timed out before completion");
            }

            notificationEventRepository.save(notificationEvent);
        }
    }
}
