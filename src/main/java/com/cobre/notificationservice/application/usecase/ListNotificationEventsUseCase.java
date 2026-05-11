package com.cobre.notificationservice.application.usecase;

import com.cobre.notificationservice.application.port.out.NotificationEventRepositoryPort;
import com.cobre.notificationservice.domain.model.NotificationEvent;
import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.DeliveryStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListNotificationEventsUseCase {

    private final NotificationEventRepositoryPort notificationEventRepository;

    public ListNotificationEventsUseCase(NotificationEventRepositoryPort notificationEventRepository) {
        this.notificationEventRepository = notificationEventRepository;
    }

    public List<NotificationEvent> list(
            ClientId clientId,
            Instant fromEventCreatedAt,
            Instant toEventCreatedAt,
            DeliveryStatus deliveryStatus) {
        return notificationEventRepository.findByClientAndFilters(
                clientId,
                fromEventCreatedAt,
                toEventCreatedAt,
                deliveryStatus);
    }
}

