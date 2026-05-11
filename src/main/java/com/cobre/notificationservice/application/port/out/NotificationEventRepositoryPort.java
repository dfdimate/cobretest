package com.cobre.notificationservice.application.port.out;

import com.cobre.notificationservice.domain.model.NotificationEvent;
import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.DeliveryStatus;
import com.cobre.notificationservice.domain.model.value.NotificationEventId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotificationEventRepositoryPort {

    NotificationEvent save(NotificationEvent notificationEvent);

    List<NotificationEvent> findByClientAndFilters(
            ClientId clientId,
            Instant fromEventCreatedAt,
            Instant toEventCreatedAt,
            DeliveryStatus deliveryStatus);

    Optional<NotificationEvent> findById(NotificationEventId notificationEventId);

    List<NotificationEventId> findDueForDelivery(Instant now);

    Optional<NotificationEvent> claimForDelivery(NotificationEventId notificationEventId, Instant claimedAt, Instant now);

    Optional<NotificationEvent> requeueIfFailed(NotificationEventId notificationEventId, Instant requeuedAt);

    List<NotificationEvent> findStaleDeliveries(Instant claimedBefore);
}
