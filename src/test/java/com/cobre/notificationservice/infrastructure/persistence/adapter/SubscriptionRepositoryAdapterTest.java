package com.cobre.notificationservice.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.EventType;
import com.cobre.notificationservice.infrastructure.persistence.entity.SubscriptionEntity;
import com.cobre.notificationservice.infrastructure.persistence.repository.SpringDataSubscriptionRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SubscriptionRepositoryAdapterTest {

    @Test
    void shouldMapSubscriptionEntityToDomainObject() {
        SpringDataSubscriptionRepository repository = mock(SpringDataSubscriptionRepository.class);
        SubscriptionRepositoryAdapter adapter = new SubscriptionRepositoryAdapter(repository);
        SubscriptionEntity entity = new SubscriptionEntity();
        entity.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        entity.setClientId("CLIENT001");
        entity.setEventType("credit_card_payment");
        entity.setTargetUrl("https://client001.example.com/webhooks/credit-card-payment");
        entity.setActive(true);
        entity.setCreatedAt(Instant.parse("2024-03-15T09:30:23Z"));
        entity.setUpdatedAt(Instant.parse("2024-03-15T09:30:24Z"));
        when(repository.findByClientIdAndEventTypeAndActiveTrue("CLIENT001", "credit_card_payment"))
                .thenReturn(Optional.of(entity));

        var subscription = adapter.findActiveByClientIdAndEventType(
                new ClientId("CLIENT001"),
                new EventType("credit_card_payment"));

        assertThat(subscription).isPresent();
        assertThat(subscription.orElseThrow().targetUrl())
                .isEqualTo("https://client001.example.com/webhooks/credit-card-payment");
    }

    @Test
    void shouldReturnEmptyWhenNoSubscriptionExists() {
        SpringDataSubscriptionRepository repository = mock(SpringDataSubscriptionRepository.class);
        SubscriptionRepositoryAdapter adapter = new SubscriptionRepositoryAdapter(repository);
        when(repository.findByClientIdAndEventTypeAndActiveTrue("CLIENT009", "unknown")).thenReturn(Optional.empty());

        var subscription = adapter.findActiveByClientIdAndEventType(
                new ClientId("CLIENT009"),
                new EventType("unknown"));

        assertThat(subscription).isEmpty();
    }
}
