package com.cobre.notificationservice.application.usecase;

import com.cobre.notificationservice.application.port.out.NotificationEventRepositoryPort;
import com.cobre.notificationservice.domain.model.NotificationEvent;
import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.NotificationEventId;
import org.springframework.stereotype.Service;

@Service
public class GetNotificationEventDetailUseCase {

    private final NotificationEventRepositoryPort notificationEventRepository;

    public GetNotificationEventDetailUseCase(NotificationEventRepositoryPort notificationEventRepository) {
        this.notificationEventRepository = notificationEventRepository;
    }

    public NotificationEvent get(NotificationEventId notificationEventId, ClientId clientId) {
        return notificationEventRepository.findById(notificationEventId)
                .filter(notificationEvent -> notificationEvent.clientId().equals(clientId))
                .orElseThrow(() -> new NotificationEventNotFoundException(notificationEventId.value()));
    }
}

