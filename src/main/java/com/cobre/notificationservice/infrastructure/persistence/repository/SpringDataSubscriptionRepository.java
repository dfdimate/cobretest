package com.cobre.notificationservice.infrastructure.persistence.repository;

import com.cobre.notificationservice.infrastructure.persistence.entity.SubscriptionEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSubscriptionRepository extends JpaRepository<SubscriptionEntity, UUID> {

    Optional<SubscriptionEntity> findByClientIdAndEventTypeAndActiveTrue(String clientId, String eventType);
}

