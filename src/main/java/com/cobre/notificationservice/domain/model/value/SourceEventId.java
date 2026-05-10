package com.cobre.notificationservice.domain.model.value;

public record SourceEventId(String value) {

    public SourceEventId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("sourceEventId cannot be blank");
        }
    }
}

