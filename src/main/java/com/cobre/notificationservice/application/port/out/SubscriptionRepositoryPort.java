package com.cobre.notificationservice.application.port.out;

import com.cobre.notificationservice.domain.model.Subscription;
import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.EventType;
import java.util.Optional;

public interface SubscriptionRepositoryPort {

    Optional<Subscription> findActiveByClientIdAndEventType(ClientId clientId, EventType eventType);
}

