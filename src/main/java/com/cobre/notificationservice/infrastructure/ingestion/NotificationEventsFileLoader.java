package com.cobre.notificationservice.infrastructure.ingestion;

import com.cobre.notificationservice.domain.model.SourceEvent;
import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.EventType;
import com.cobre.notificationservice.domain.model.value.SourceEventId;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventsFileLoader {

    private final ObjectMapper objectMapper;

    public NotificationEventsFileLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<SourceEvent> load() {
        ClassPathResource resource = new ClassPathResource("notification_events.json");

        try (InputStream inputStream = resource.getInputStream()) {
            NotificationEventsPayload payload = objectMapper.readValue(inputStream, NotificationEventsPayload.class);
            return payload.events().stream()
                    .map(event -> new SourceEvent(
                            new SourceEventId(event.eventId()),
                            new ClientId(event.clientId()),
                            new EventType(event.eventType()),
                            event.content(),
                            Instant.parse(event.deliveryDate())))
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load notification_events.json", exception);
        }
    }

    private record NotificationEventsPayload(List<NotificationEventFileRecord> events) {
    }

    private record NotificationEventFileRecord(
            @JsonProperty("event_id") String eventId,
            @JsonProperty("event_type") String eventType,
            @JsonProperty("content") String content,
            @JsonProperty("delivery_date") String deliveryDate,
            @JsonProperty("client_id") String clientId) {
    }
}

