package com.cobre.notificationservice.infrastructure.persistence.adapter;

import com.cobre.notificationservice.application.port.out.SubscriptionRepositoryPort;
import com.cobre.notificationservice.domain.model.Subscription;
import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.EventType;
import com.cobre.notificationservice.infrastructure.persistence.repository.SpringDataSubscriptionRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class SubscriptionRepositoryAdapter implements SubscriptionRepositoryPort {

    private final SpringDataSubscriptionRepository repository;

    public SubscriptionRepositoryAdapter(SpringDataSubscriptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Subscription> findActiveByClientIdAndEventType(ClientId clientId, EventType eventType) {
        return repository.findByClientIdAndEventTypeAndActiveTrue(clientId.value(), eventType.value())
                .map(entity -> new Subscription(
                        new ClientId(entity.getClientId()),
                        new EventType(entity.getEventType()),
                        entity.getTargetUrl(),
                        entity.isActive()));
    }
}

