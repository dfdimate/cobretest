package com.cobre.notificationservice.domain.model.value;

public record ClientId(String value) {

    public ClientId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("clientId cannot be blank");
        }
    }
}

