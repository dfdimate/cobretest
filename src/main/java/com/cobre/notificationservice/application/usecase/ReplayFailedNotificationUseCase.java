package com.cobre.notificationservice.application.usecase;

import com.cobre.notificationservice.application.port.out.ClockPort;
import com.cobre.notificationservice.application.port.out.NotificationEventRepositoryPort;
import com.cobre.notificationservice.domain.model.NotificationEvent;
import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.NotificationEventId;
import org.springframework.stereotype.Service;

@Service
public class ReplayFailedNotificationUseCase {

    private final NotificationEventRepositoryPort notificationEventRepository;
    private final ClockPort clockPort;

    public ReplayFailedNotificationUseCase(
            NotificationEventRepositoryPort notificationEventRepository,
            ClockPort clockPort) {
        this.notificationEventRepository = notificationEventRepository;
        this.clockPort = clockPort;
    }

    public NotificationEvent replay(NotificationEventId notificationEventId, ClientId clientId) {
        NotificationEvent notificationEvent = notificationEventRepository.findById(notificationEventId)
                .filter(event -> event.clientId().equals(clientId))
                .orElseThrow(() -> new NotificationEventNotFoundException(notificationEventId.value()));

        if (!notificationEvent.canReplay()) {
            throw new ReplayNotAllowedException(notificationEventId.value());
        }

        clockPort.now();
        notificationEvent.requeue();
        return notificationEventRepository.save(notificationEvent);
    }
}

