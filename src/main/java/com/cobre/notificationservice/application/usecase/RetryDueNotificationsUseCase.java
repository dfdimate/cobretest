package com.cobre.notificationservice.application.usecase;

import com.cobre.notificationservice.application.port.out.ClockPort;
import com.cobre.notificationservice.application.port.out.NotificationEventRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class RetryDueNotificationsUseCase {

    private final NotificationEventRepositoryPort notificationEventRepository;
    private final ProcessNotificationDeliveryUseCase processNotificationDeliveryUseCase;
    private final ClockPort clockPort;

    public RetryDueNotificationsUseCase(
            NotificationEventRepositoryPort notificationEventRepository,
            ProcessNotificationDeliveryUseCase processNotificationDeliveryUseCase,
            ClockPort clockPort) {
        this.notificationEventRepository = notificationEventRepository;
        this.processNotificationDeliveryUseCase = processNotificationDeliveryUseCase;
        this.clockPort = clockPort;
    }

    public void processDueNotifications() {
        notificationEventRepository.findDueForDelivery(clockPort.now()).forEach(notificationEvent ->
                processNotificationDeliveryUseCase.process(notificationEvent.notificationEventId()));
    }
}

