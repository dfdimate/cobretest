package com.cobre.notificationservice.infrastructure.persistence.adapter;

import com.cobre.notificationservice.application.port.out.NotificationEventRepositoryPort;
import com.cobre.notificationservice.domain.model.NotificationEvent;
import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.DeliveryStatus;
import com.cobre.notificationservice.domain.model.value.EventType;
import com.cobre.notificationservice.domain.model.value.NotificationEventId;
import com.cobre.notificationservice.domain.model.value.SourceEventId;
import com.cobre.notificationservice.infrastructure.persistence.entity.NotificationEventEntity;
import com.cobre.notificationservice.infrastructure.persistence.repository.SpringDataNotificationEventRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationEventRepositoryAdapter implements NotificationEventRepositoryPort {

    private final SpringDataNotificationEventRepository repository;

    public NotificationEventRepositoryAdapter(SpringDataNotificationEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public NotificationEvent save(NotificationEvent notificationEvent) {
        NotificationEventEntity entity = toEntity(notificationEvent);
        return toDomain(repository.save(entity));
    }

    @Override
    public List<NotificationEvent> findByClientAndFilters(
            ClientId clientId,
            Instant fromEventCreatedAt,
            Instant toEventCreatedAt,
            DeliveryStatus deliveryStatus) {
        return repository.findByFilters(clientId.value(), fromEventCreatedAt, toEventCreatedAt, deliveryStatus)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<NotificationEvent> findById(NotificationEventId notificationEventId) {
        return repository.findById(notificationEventId.value()).map(this::toDomain);
    }

    @Override
    public List<NotificationEvent> findDueForDelivery(Instant now) {
        return repository.findDueForDelivery(now).stream()
                .map(this::toDomain)
                .toList();
    }

    private NotificationEventEntity toEntity(NotificationEvent notificationEvent) {
        NotificationEventEntity entity = new NotificationEventEntity();
        entity.setNotificationEventId(notificationEvent.notificationEventId().value());
        entity.setSourceEventId(notificationEvent.sourceEventId().value());
        entity.setClientId(notificationEvent.clientId().value());
        entity.setEventType(notificationEvent.eventType().value());
        entity.setContent(notificationEvent.content());
        entity.setEventCreatedAt(notificationEvent.eventCreatedAt());
        entity.setDeliveryStatus(notificationEvent.deliveryStatus());
        entity.setAttemptCount(notificationEvent.attemptCount());
        entity.setLastAttemptAt(notificationEvent.lastAttemptAt());
        entity.setDeliveredAt(notificationEvent.deliveredAt());
        entity.setNextRetryAt(notificationEvent.nextRetryAt());
        entity.setHttpStatus(notificationEvent.httpStatus());
        entity.setFinalFailureReason(notificationEvent.finalFailureReason());
        entity.setCreatedAt(notificationEvent.createdAt());
        entity.setUpdatedAt(notificationEvent.lastAttemptAt() != null
                ? notificationEvent.lastAttemptAt()
                : notificationEvent.createdAt());
        return entity;
    }

    private NotificationEvent toDomain(NotificationEventEntity entity) {
        return NotificationEvent.restore(
                new NotificationEventId(entity.getNotificationEventId()),
                new SourceEventId(entity.getSourceEventId()),
                new ClientId(entity.getClientId()),
                new EventType(entity.getEventType()),
                entity.getContent(),
                entity.getEventCreatedAt(),
                entity.getCreatedAt(),
                entity.getDeliveryStatus(),
                entity.getAttemptCount(),
                entity.getLastAttemptAt(),
                entity.getDeliveredAt(),
                entity.getNextRetryAt(),
                entity.getHttpStatus(),
                entity.getFinalFailureReason());
    }
}
