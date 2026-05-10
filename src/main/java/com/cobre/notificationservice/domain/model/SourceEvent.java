package com.cobre.notificationservice.domain.model;

import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.EventType;
import com.cobre.notificationservice.domain.model.value.SourceEventId;
import java.time.Instant;

public record SourceEvent(
        SourceEventId sourceEventId,
        ClientId clientId,
        EventType eventType,
        String content,
        Instant eventCreatedAt) {
}

