package com.cobre.notificationservice.infrastructure.api.dto;

import java.util.List;

public record NotificationEventsResponse(List<NotificationEventSummaryResponse> items) {
}

