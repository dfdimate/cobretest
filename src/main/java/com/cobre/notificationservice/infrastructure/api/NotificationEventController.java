package com.cobre.notificationservice.infrastructure.api;

import com.cobre.notificationservice.application.usecase.GetNotificationEventDetailUseCase;
import com.cobre.notificationservice.application.usecase.ListNotificationEventsUseCase;
import com.cobre.notificationservice.application.usecase.ReplayFailedNotificationUseCase;
import com.cobre.notificationservice.domain.model.NotificationEvent;
import com.cobre.notificationservice.domain.model.value.DeliveryStatus;
import com.cobre.notificationservice.domain.model.value.NotificationEventId;
import com.cobre.notificationservice.infrastructure.api.dto.NotificationEventDetailResponse;
import com.cobre.notificationservice.infrastructure.api.dto.NotificationEventSummaryResponse;
import com.cobre.notificationservice.infrastructure.api.dto.NotificationEventsResponse;
import com.cobre.notificationservice.infrastructure.api.dto.ReplayNotificationResponse;
import com.cobre.notificationservice.infrastructure.security.DemoClientIdentityResolver;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationEventController {

    private final ListNotificationEventsUseCase listNotificationEventsUseCase;
    private final GetNotificationEventDetailUseCase getNotificationEventDetailUseCase;
    private final ReplayFailedNotificationUseCase replayFailedNotificationUseCase;
    private final DemoClientIdentityResolver demoClientIdentityResolver;

    public NotificationEventController(
            ListNotificationEventsUseCase listNotificationEventsUseCase,
            GetNotificationEventDetailUseCase getNotificationEventDetailUseCase,
            ReplayFailedNotificationUseCase replayFailedNotificationUseCase,
            DemoClientIdentityResolver demoClientIdentityResolver) {
        this.listNotificationEventsUseCase = listNotificationEventsUseCase;
        this.getNotificationEventDetailUseCase = getNotificationEventDetailUseCase;
        this.replayFailedNotificationUseCase = replayFailedNotificationUseCase;
        this.demoClientIdentityResolver = demoClientIdentityResolver;
    }

    @GetMapping("/notification_events")
    public NotificationEventsResponse list(
            @RequestParam(name = "from_event_created_at", required = false) Instant fromEventCreatedAt,
            @RequestParam(name = "to_event_created_at", required = false) Instant toEventCreatedAt,
            @RequestParam(name = "delivery_status", required = false) DeliveryStatus deliveryStatus,
            HttpServletRequest request) {
        List<NotificationEventSummaryResponse> items = listNotificationEventsUseCase
                .list(
                        demoClientIdentityResolver.resolveClientId(request),
                        fromEventCreatedAt,
                        toEventCreatedAt,
                        deliveryStatus)
                .stream()
                .map(this::toSummary)
                .toList();

        return new NotificationEventsResponse(items);
    }

    @GetMapping("/notification_events/{notificationEventId}")
    public NotificationEventDetailResponse getById(
            @PathVariable String notificationEventId,
            HttpServletRequest request) {
        NotificationEvent notificationEvent = getNotificationEventDetailUseCase.get(
                new NotificationEventId(notificationEventId),
                demoClientIdentityResolver.resolveClientId(request));
        return toDetail(notificationEvent);
    }

    @PostMapping("/notification_events/{notificationEventId}/replay")
    public ResponseEntity<ReplayNotificationResponse> replay(
            @PathVariable String notificationEventId,
            HttpServletRequest request) {
        NotificationEvent notificationEvent = replayFailedNotificationUseCase.replay(
                new NotificationEventId(notificationEventId),
                demoClientIdentityResolver.resolveClientId(request));
        return ResponseEntity.accepted().body(new ReplayNotificationResponse(
                notificationEvent.notificationEventId().value(),
                notificationEvent.deliveryStatus().name(),
                "Notification replay scheduled"));
    }

    private NotificationEventSummaryResponse toSummary(NotificationEvent notificationEvent) {
        return new NotificationEventSummaryResponse(
                notificationEvent.notificationEventId().value(),
                notificationEvent.sourceEventId().value(),
                notificationEvent.eventType().value(),
                notificationEvent.content(),
                notificationEvent.eventCreatedAt(),
                notificationEvent.deliveryStatus().name(),
                notificationEvent.deliveredAt(),
                notificationEvent.lastAttemptAt());
    }

    private NotificationEventDetailResponse toDetail(NotificationEvent notificationEvent) {
        return new NotificationEventDetailResponse(
                notificationEvent.notificationEventId().value(),
                notificationEvent.sourceEventId().value(),
                notificationEvent.eventType().value(),
                notificationEvent.content(),
                notificationEvent.eventCreatedAt(),
                notificationEvent.deliveryStatus().name(),
                notificationEvent.deliveredAt(),
                notificationEvent.lastAttemptAt(),
                notificationEvent.attemptCount(),
                notificationEvent.httpStatus(),
                notificationEvent.finalFailureReason());
    }
}

