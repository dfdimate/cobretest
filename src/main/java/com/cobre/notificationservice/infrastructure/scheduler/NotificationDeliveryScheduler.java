package com.cobre.notificationservice.infrastructure.scheduler;

import com.cobre.notificationservice.application.usecase.RetryDueNotificationsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationDeliveryScheduler {

    private final RetryDueNotificationsUseCase retryDueNotificationsUseCase;

    public NotificationDeliveryScheduler(RetryDueNotificationsUseCase retryDueNotificationsUseCase) {
        this.retryDueNotificationsUseCase = retryDueNotificationsUseCase;
    }

    @Scheduled(fixedDelayString = "${notifications.delivery.poll-delay:5000}")
    public void deliverDueNotifications() {
        retryDueNotificationsUseCase.processDueNotifications();
    }
}
