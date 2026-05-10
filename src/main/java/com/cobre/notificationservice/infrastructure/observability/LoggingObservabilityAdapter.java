package com.cobre.notificationservice.infrastructure.observability;

import com.cobre.notificationservice.application.port.out.ObservabilityPort;
import com.cobre.notificationservice.domain.model.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingObservabilityAdapter implements ObservabilityPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingObservabilityAdapter.class);

    @Override
    public void recordCompleted(NotificationEvent notificationEvent) {
        log.info(
                "notification_delivery_completed notificationEventId={} clientId={} eventType={} attemptCount={} httpStatus={}",
                notificationEvent.notificationEventId().value(),
                notificationEvent.clientId().value(),
                notificationEvent.eventType().value(),
                notificationEvent.attemptCount(),
                notificationEvent.httpStatus());
    }

    @Override
    public void recordRetryableFailure(NotificationEvent notificationEvent) {
        log.warn(
                "notification_delivery_retryable_failure notificationEventId={} clientId={} eventType={} attemptCount={} nextRetryAt={} error={}",
                notificationEvent.notificationEventId().value(),
                notificationEvent.clientId().value(),
                notificationEvent.eventType().value(),
                notificationEvent.attemptCount(),
                notificationEvent.nextRetryAt(),
                notificationEvent.finalFailureReason());
    }

    @Override
    public void recordFailed(NotificationEvent notificationEvent) {
        log.error(
                "notification_delivery_failed notificationEventId={} clientId={} eventType={} attemptCount={} httpStatus={} error={}",
                notificationEvent.notificationEventId().value(),
                notificationEvent.clientId().value(),
                notificationEvent.eventType().value(),
                notificationEvent.attemptCount(),
                notificationEvent.httpStatus(),
                notificationEvent.finalFailureReason());
    }
}

